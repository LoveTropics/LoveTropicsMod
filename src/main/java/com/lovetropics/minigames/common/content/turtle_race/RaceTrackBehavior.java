package com.lovetropics.minigames.common.content.turtle_race;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.GameBossBar;
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
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RaceTrackBehavior(PathData path) implements IGameBehavior {
	public static final Codec<RaceTrackBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			PathData.CODEC.fieldOf("path").forGetter(RaceTrackBehavior::path)
	).apply(i, RaceTrackBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		Component gameName = game.getDefinition().getName().copy().withStyle(ChatFormatting.AQUA);
		RaceTrackPath path = this.path.compile(game.getMapRegions());

		Map<UUID, PlayerState> states = new Object2ObjectOpenHashMap<>();

		events.listen(GamePlayerEvents.SET_ROLE, (player, role, lastRole) -> {
			if (role == PlayerRole.PARTICIPANT) {
				states.put(player.getUUID(), new PlayerState());
			}
		});

		events.listen(GamePlayerEvents.TICK, player -> {
			PlayerState state = states.get(player.getUUID());
			if (state == null) {
				return;
			}

			int x = player.getBlockX();
			int z = player.getBlockZ();
			if (state.tracker.tryUpdate(x, z, game.ticks())) {
				RaceTrackPath.Point point = path.closestPointAt(x, z);
				state.trackProgress(point.position() / path.length());
				state.updateBar(player, gameName);
			}
		});

		events.listen(GamePlayerEvents.REMOVE, player -> {
			PlayerState state = states.remove(player.getUUID());
			if (state != null) {
				state.close();
			}
		});
	}

	private static class PlayerState implements AutoCloseable {
		@Nullable
		private GameBossBar bar;

		private float trackedProgress;
		private final Tracker tracker = new Tracker();

		public void trackProgress(float progress) {
			if (progress > trackedProgress) {
				trackedProgress = progress;
			}
		}

		public void updateBar(ServerPlayer player, Component text) {
			if (bar == null) {
				bar = new GameBossBar(text, BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);
				bar.addPlayer(player);
			} else {
				bar.setTitle(text);
			}
			bar.setProgress(trackedProgress);
		}

		@Override
		public void close() {
			if (bar != null) {
				bar.close();
			}
		}
	}

	private static class Tracker {
		private static final long TRACK_INTERVAL = SharedConstants.TICKS_PER_SECOND;
		private static final int TRACK_MOVE_THRESHOLD = 5;

		private int trackedX;
		private int trackedZ;
		private long trackedTime;
		private boolean tracked;

		public boolean tryUpdate(int x, int z, long time) {
			if (canUpdate(x, z, time)) {
				trackedX = x;
				trackedZ = z;
				trackedTime = time;
				tracked = true;
				return true;
			}
			return false;
		}

		private boolean canUpdate(int x, int z, long time) {
			if (!tracked) {
				return true;
			}
			if (time - trackedTime >= TRACK_INTERVAL) {
				int deltaX = x - trackedX;
				int deltaZ = z - trackedZ;
				return Math.abs(deltaX) + Math.abs(deltaZ) > TRACK_MOVE_THRESHOLD;
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
			if (positions.isEmpty()) {
				throw new GameException(new TextComponent("No track path points found"));
			}

			BlockPos start = positions.get(0);
			path.addPoint(start.getX(), start.getZ(), 0.0f);

			float position = 0.0f;
			for (int i = 1; i < positions.size(); i++) {
				BlockPos lastPoint = positions.get(i - 1);
				BlockPos point = positions.get(i);

				int deltaX = point.getX() - lastPoint.getX();
				int deltaZ = point.getZ() - lastPoint.getZ();
				float length = Mth.sqrt(deltaX * deltaX + deltaZ * deltaZ);
				position += length;

				path.addPoint(point.getX(), point.getZ(), position);
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
