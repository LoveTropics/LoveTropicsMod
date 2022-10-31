package com.lovetropics.minigames.common.content.turtle_race;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.GameBossBar;
import com.lovetropics.minigames.common.core.game.util.GameSidebar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class RaceTrackBehavior implements IGameBehavior {
	public static final Codec<RaceTrackBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			PathData.CODEC.fieldOf("path").forGetter(b -> b.pathData),
			Codec.STRING.optionalFieldOf("finish_region", "finish").forGetter(b -> b.finishRegion)
	).apply(i, RaceTrackBehavior::new));

	private static final int SIDEBAR_UPDATE_INTERVAL = SharedConstants.TICKS_PER_SECOND;
	private static final int MAX_LEADERBOARD_SIZE = 5;

	private final PathData pathData;
	private final String finishRegion;

	private final Map<UUID, PlayerState> states = new Object2ObjectOpenHashMap<>();

	private IGamePhase game;

	private RaceTrackPath path;
	private AABB finishBox;

	public RaceTrackBehavior(PathData pathData, String finishRegion) {
		this.pathData = pathData;
		this.finishRegion = finishRegion;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		this.game = game;
		path = pathData.compile(game.getMapRegions());

		finishBox = game.getMapRegions().getOrThrow(finishRegion).asAabb();

		GlobalGameWidgets widgets = GlobalGameWidgets.registerTo(game, events);
		GameSidebar sidebar = widgets.openSidebar(game.getDefinition().getName().copy().withStyle(ChatFormatting.AQUA));

		events.listen(GamePlayerEvents.SPAWN, (player, role) -> {
			if (role == PlayerRole.PARTICIPANT) {
				states.put(player.getUUID(), new PlayerState(player.position(), game.ticks()));
			}
		});

		events.listen(GamePlayerEvents.TICK, player -> {
			PlayerState state = states.get(player.getUUID());
			if (state == null) {
				return;
			}

			Vec3 position = player.position();
			Vec3 lastPosition = state.tracker.tryUpdate(position, game.ticks());
			if (lastPosition != null) {
				onPlayerMove(player, state, position, lastPosition);
			}
		});

		events.listen(GamePlayerEvents.REMOVE, player -> {
			PlayerState state = states.remove(player.getUUID());
			if (state != null) {
				state.close();
			}
		});

		events.listen(GamePhaseEvents.TICK, () -> {
			if (game.ticks() % SIDEBAR_UPDATE_INTERVAL == 0) {
				sidebar.set(buildSidebar());
			}
		});
	}

	private void onPlayerMove(ServerPlayer player, PlayerState state, Vec3 position, Vec3 lastPosition) {
		if (state.trackedPosition >= 0.9f && finishBox.clip(lastPosition, position).isPresent()) {
			state.nextLap();
		}

		RaceTrackPath.Point point = path.closestPointAt(player.getBlockX(), player.getBlockZ(), state.trackedPosition);
		state.trackPosition(point.position());

		Component title = new TextComponent("Lap #" + (state.lap + 1)).withStyle(ChatFormatting.AQUA);
		state.updateBar(player, title, path.length());
	}

	private String[] buildSidebar() {
		record Entry(String name, int lap, float position) {
		}

		List<Entry> leaderboard = game.getParticipants().stream()
				.map(player -> {
					PlayerState state = states.get(player.getUUID());
					if (state != null) {
						return new Entry(player.getGameProfile().getName(), state.lap, state.trackedPosition);
					}
					return null;
				})
				.filter(Objects::nonNull)
				.sorted(Comparator.comparingInt(Entry::lap).thenComparingDouble(Entry::position).reversed())
				.limit(MAX_LEADERBOARD_SIZE)
				.toList();

		String[] lines = new String[leaderboard.size()];
		for (int i = 0; i < leaderboard.size(); i++) {
			Entry entry = leaderboard.get(i);
			int percent = Math.round(entry.position() / path.length() * 100.0f);
			lines[i] = ChatFormatting.GRAY.toString() + (i + 1) + ". " + ChatFormatting.GOLD + entry.name() + ChatFormatting.GRAY + " (" + percent + "%)";
		}

		return lines;
	}

	private static class PlayerState implements AutoCloseable {
		@Nullable
		private GameBossBar bar;

		private int lap;
		private float trackedPosition;

		private final Tracker tracker;

		private PlayerState(Vec3 position, long time) {
			tracker = new Tracker(position, time);
		}

		public int nextLap() {
			trackedPosition = 0.0f;
			return ++lap;
		}

		public void trackPosition(float position) {
			if (position > trackedPosition) {
				trackedPosition = position;
			}
		}

		public void updateBar(ServerPlayer player, Component text, float pathLength) {
			if (bar == null) {
				bar = new GameBossBar(text, BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
				bar.addPlayer(player);
			} else {
				bar.setTitle(text);
			}
			bar.setProgress(trackedPosition / pathLength);
		}

		@Override
		public void close() {
			if (bar != null) {
				bar.close();
			}
		}
	}

	private static class Tracker {
		private static final long MIN_TRACK_INTERVAL = SharedConstants.TICKS_PER_SECOND / 2;
		private static final int TRACK_MOVE_THRESHOLD = 3;

		private Vec3 trackedPosition;
		private long trackedTime;

		public Tracker(Vec3 position, long time) {
			trackedPosition = position;
			trackedTime = time;
		}

		@Nullable
		public Vec3 tryUpdate(Vec3 position, long time) {
			if (canUpdate(position, time)) {
				Vec3 lastPosition = trackedPosition;
				trackedPosition = position;
				trackedTime = time;
				return lastPosition;
			}
			return null;
		}

		private boolean canUpdate(Vec3 position, long time) {
			if (time - trackedTime >= MIN_TRACK_INTERVAL) {
				return !position.closerThan(trackedPosition, TRACK_MOVE_THRESHOLD);
			}
			return false;
		}
	}

	private record PathData(int start, int end, String prefix) {
		public static final Codec<PathData> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.INT.fieldOf("start").forGetter(PathData::start),
				Codec.INT.fieldOf("end").forGetter(PathData::end),
				Codec.STRING.fieldOf("prefix").forGetter(PathData::prefix)
		).apply(i, PathData::new));

		public RaceTrackPath compile(MapRegions regions) {
			RaceTrackPath.Builder path = RaceTrackPath.builder();

			List<BlockPos> positions = collectPositions(regions);
			for (BlockPos point : positions) {
				path.addPoint(point.getX(), point.getZ());
			}

			return path.build();
		}

		private List<BlockPos> collectPositions(MapRegions regions) {
			List<BlockPos> positions = new ArrayList<>();
			for (int index = start; index <= end; index++) {
				BlockBox box = regions.getAny(prefix + index);
				if (box != null) {
					positions.add(box.centerBlock());
				}
			}
			return positions;
		}
	}
}
