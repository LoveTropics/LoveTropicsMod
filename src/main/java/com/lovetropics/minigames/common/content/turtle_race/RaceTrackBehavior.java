package com.lovetropics.minigames.common.content.turtle_race;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.lib.entity.FireworkPalette;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.util.GameBossBar;
import com.lovetropics.minigames.common.core.game.util.GameSidebar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// TODO: Can this behaviour be split up at all?
public class RaceTrackBehavior implements IGameBehavior {
	public static final Codec<RaceTrackBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			PathData.CODEC.fieldOf("path").forGetter(b -> b.pathData),
			Codec.STRING.optionalFieldOf("finish_region", "finish").forGetter(b -> b.finishRegion),
			Codec.INT.optionalFieldOf("lap_count", 3).forGetter(b -> b.lapCount),
			Codec.INT.optionalFieldOf("winner_count", 3).forGetter(b -> b.winnerCount),
			Codec.LONG.optionalFieldOf("start_time", 0L).forGetter(b -> b.startTime)
	).apply(i, RaceTrackBehavior::new));

	private static final int SIDEBAR_UPDATE_INTERVAL = SharedConstants.TICKS_PER_SECOND;
	private static final int MAX_LEADERBOARD_SIZE = 5;

	private static final int STUCK_WARNING_THRESHOLD = SharedConstants.TICKS_PER_SECOND * 5;
	private static final int STUCK_WARNING_REPEAT_INTERVAL = SharedConstants.TICKS_PER_SECOND / 2;

	private static final Component UNKNOWN_PLAYER_NAME = new TextComponent("Unknown");

	private final PathData pathData;
	private final String finishRegion;
	private final int lapCount;
	private final int winnerCount;
	// TODO: Should be a phase / other kind of trigger
	private final long startTime;

	private final Map<UUID, PlayerState> states = new Object2ObjectOpenHashMap<>();
	private final List<FinishEntry> finishedPlayers = new ArrayList<>();

	private IGamePhase game;

	private RaceTrackPath path;
	private AABB finishBox;

	public RaceTrackBehavior(PathData pathData, String finishRegion, int lapCount, int winnerCount, long startTime) {
		this.pathData = pathData;
		this.finishRegion = finishRegion;
		this.lapCount = lapCount;
		this.winnerCount = winnerCount;
		this.startTime = startTime;
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
			long gameTime = game.ticks();
			PlayerState state = states.get(player.getUUID());
			if (state == null || gameTime < startTime) {
				return;
			}

			Vec3 position = player.position();
			Vec3 lastPosition = state.tracker.tryUpdate(position, gameTime);
			if (lastPosition != null && onPlayerMove(player, state, position, lastPosition)) {
				onPlayerFinish(game, player, state);
			}

			if (gameTime % STUCK_WARNING_REPEAT_INTERVAL == 0) {
				long stuckTime = gameTime - state.lastMovedTime;
				if (stuckTime > STUCK_WARNING_THRESHOLD) {
					showStuckWarning(player);
				}
			}
		});

		events.listen(GamePlayerEvents.REMOVE, this::clearPlayerState);

		events.listen(GamePhaseEvents.TICK, () -> {
			if (game.ticks() % SIDEBAR_UPDATE_INTERVAL == 0) {
				sidebar.set(buildSidebar());
			}
		});
	}

	private void showStuckWarning(ServerPlayer player) {
		player.connection.send(new ClientboundSetTitlesAnimationPacket(0, STUCK_WARNING_REPEAT_INTERVAL * 2, 10));
		player.connection.send(new ClientboundSetTitleTextPacket(new TextComponent("Warning!").withStyle(ChatFormatting.RED)));
		player.connection.send(new ClientboundSetSubtitleTextPacket(new TextComponent("You are going the wrong way!").withStyle(ChatFormatting.GOLD)));
	}

	private void clearPlayerState(ServerPlayer player) {
		PlayerState state = states.remove(player.getUUID());
		if (state != null) {
			state.close();
		}
	}

	private void triggerWin(IGamePhase game) {
		Component winnerName = UNKNOWN_PLAYER_NAME;
		if (!finishedPlayers.isEmpty()) {
			FinishEntry winner = finishedPlayers.get(0);
			game.getStatistics().global().set(StatisticKey.WINNING_PLAYER, winner.player());
			winnerName = new TextComponent(winner.name());
		}

		game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(winnerName);

		for (ServerPlayer player : game.getParticipants()) {
			clearPlayerState(player);
		}
	}

	private boolean onPlayerMove(ServerPlayer player, PlayerState state, Vec3 position, Vec3 lastPosition) {
		// TODO: Not a great way to detect this
		if (state.trackedPosition >= 0.9f * path.length() && finishBox.clip(lastPosition, position).isPresent()) {
			if (onPlayerFinishLap(player, state)) {
				return true;
			}
		}

		Vec3 delta = position.subtract(lastPosition);
		double movedDistance = delta.horizontalDistance();

		RaceTrackPath.Point point = path.nextPointAt(player.getBlockX(), player.getBlockZ(), state.trackedPosition, (float) (movedDistance * 2.0f));
		state.trackPosition(point.position(), game.ticks());

		Component title = new TextComponent("Lap #" + (state.lap + 1) + " of " + lapCount).withStyle(ChatFormatting.AQUA);
		state.updateBar(player, title, path.length());

		return false;
	}

	private boolean onPlayerFinishLap(ServerPlayer player, PlayerState state) {
		FireworkPalette.DYE_COLORS.spawn(player.blockPosition(), player.level);

		long lapTime = game.ticks() - state.lapStartTime;
		game.getAllPlayers().sendMessage(new TextComponent("")
				.append(new TextComponent("\u2714 ").withStyle(ChatFormatting.GREEN))
				.append(player.getDisplayName())
				.append(" finished lap #" + (state.lap + 1) + " in ")
				.append(new TextComponent(Util.formatMinutesSeconds(lapTime)).withStyle(ChatFormatting.GOLD))
				.append("!")
				.withStyle(ChatFormatting.AQUA)
		);
		game.getAllPlayers().playSound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 1.0f, 1.0f);

		return state.nextLap(game.ticks()) >= lapCount;
	}

	private void onPlayerFinish(IGamePhase game, ServerPlayer player, PlayerState state) {
		long time = game.ticks() - startTime;
		finishedPlayers.add(new FinishEntry(player.getGameProfile().getName(), PlayerKey.from(player), time));

		game.getStatistics().forPlayer(player)
				.set(StatisticKey.PLACEMENT, finishedPlayers.size())
				.set(StatisticKey.TOTAL_TIME, (int) time);

		game.setPlayerRole(player, PlayerRole.SPECTATOR);

		states.remove(player.getUUID(), state);
		state.close();

		// TODO: Start a countdown instead of immediate end
		if (game.getParticipants().isEmpty() || finishedPlayers.size() >= winnerCount) {
			triggerWin(game);
		}
	}

	private String[] buildSidebar() {
		class Leaderboard {
			private final List<String> lines = new ArrayList<>(MAX_LEADERBOARD_SIZE);

			public void add(String player, String detail) {
				if (lines.size() < MAX_LEADERBOARD_SIZE) {
					String index = (lines.size() + 1) + ". ";
					lines.add(ChatFormatting.GRAY + index + ChatFormatting.GOLD + player + " " + detail);
				}
			}

			public String[] build() {
				return lines.toArray(String[]::new);
			}
		}

		Leaderboard leaderboard = new Leaderboard();

		for (FinishEntry entry : finishedPlayers) {
			long seconds = entry.time() / SharedConstants.TICKS_PER_SECOND;
			leaderboard.add(entry.name(), ChatFormatting.GREEN + Util.formatMinutesSeconds(seconds) + " \u2714");
		}

		states.entrySet().stream()
				.sorted(Map.Entry.comparingByValue(Comparator.<PlayerState>comparingInt(e -> e.lap).thenComparingDouble(s -> s.trackedPosition).reversed()))
				.forEach(entry -> {
					ServerPlayer player = game.getParticipants().getPlayerBy(entry.getKey());
					PlayerState state = entry.getValue();
					if (player != null) {
						int percent = Math.round(state.trackedPosition / path.length() * 100.0f);
						leaderboard.add(player.getGameProfile().getName(), ChatFormatting.GRAY + "(Lap #" + (state.lap + 1) + "; " + percent + "%)");
					}
				});

		return leaderboard.build();
	}

	private record FinishEntry(String name, PlayerKey player, long time) {
	}

	private static class PlayerState implements AutoCloseable {
		@Nullable
		private GameBossBar bar;

		private int lap;
		private float trackedPosition;
		private long lapStartTime;

		private long lastMovedTime;

		private final Tracker tracker;

		private PlayerState(Vec3 position, long time) {
			tracker = new Tracker(position, time);
			lapStartTime = time;
		}

		public int nextLap(long time) {
			trackedPosition = 0.0f;
			lapStartTime = time;
			return ++lap;
		}

		public void trackPosition(float position, long time) {
			if (position > trackedPosition) {
				lastMovedTime = time;
			}
			trackedPosition = position;
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
