package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.GameProgressionState;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.ProgressionPeriod;
import com.lovetropics.minigames.common.core.game.state.ProgressionPoint;
import com.lovetropics.minigames.common.util.LinearSpline;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.SharedConstants;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Map;

public class GameProgressionBehavior implements IGameBehavior {
	public static final MapCodec<GameProgressionBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.unboundedMap(Codec.STRING, Codec.FLOAT).optionalFieldOf("named_points", Map.of()).forGetter(b -> b.namedPoints),
			Codec.INT.optionalFieldOf("max_time_step", 1).forGetter(b -> b.maxTimeStep),
			MoreCodecs.listOrUnit(ProgressionPeriod.CODEC).optionalFieldOf("fixed_time_step", List.of()).forGetter(b -> b.fixedTimeStep),
			PlayerConstraint.CODEC.listOf().optionalFieldOf("time_by_player_count", List.of()).forGetter(b -> b.playerConstraints)
	).apply(i, GameProgressionBehavior::new));

	private GameProgressionState progressionState;

	private final Map<String, Float> namedPoints;
	private final int maxTimeStep;
	private final List<ProgressionPeriod> fixedTimeStep;
	private final List<PlayerConstraint> playerConstraints;

	private Float2FloatFunction playerCountToTime = key -> 0.0f;

	public GameProgressionBehavior(Map<String, Float> namedPoints, int maxTimeStep, List<ProgressionPeriod> fixedTimeStep, List<PlayerConstraint> playerConstraints) {
		this.namedPoints = namedPoints;
		this.maxTimeStep = maxTimeStep;
		this.fixedTimeStep = fixedTimeStep;
		this.playerConstraints = playerConstraints;
	}

	@Override
	public void registerState(IGamePhase game, GameStateMap phaseState, GameStateMap instanceState) {
		progressionState = phaseState.register(GameProgressionState.KEY, new GameProgressionState());
		namedPoints.forEach((name, value) -> progressionState.addNamedPoint(name, Math.round(value * SharedConstants.TICKS_PER_SECOND)));
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.START, () -> {
			if (!playerConstraints.isEmpty()) {
				playerCountToTime = resolvePlayerConstraints(playerConstraints, game.getParticipants().size(), progressionState);
			}
		});

		events.listen(GamePhaseEvents.TICK, () -> {
			int newTime = tickTime(game, progressionState.time());
			progressionState.set(newTime);
		});
	}

	private int tickTime(IGamePhase game, int time) {
		if (progressionState.is(fixedTimeStep)) {
			return ++time;
		}

		int playerCount = game.getParticipants().size();

		int targetTimeByPlayerCount = Mth.ceil(playerCountToTime.get(playerCount));
		if (targetTimeByPlayerCount > time) {
			int step = Math.min(targetTimeByPlayerCount - time, maxTimeStep);
			time += step;
			return time;
		} else {
			return ++time;
		}
	}

	private static Float2FloatFunction resolvePlayerConstraints(List<PlayerConstraint> constraints, int initialPlayerCount, GameProgressionState progression) {
		LinearSpline.Builder spline = LinearSpline.builder();
		for (PlayerConstraint constraint : constraints) {
			spline.point(constraint.count().resolve(initialPlayerCount), constraint.point().resolve(progression));
		}
		return spline.build();
	}

	private record PlayerConstraint(PlayerCount count, ProgressionPoint point) {
		public static final Codec<PlayerConstraint> CODEC = RecordCodecBuilder.create(i -> i.group(
				PlayerCount.CODEC.forGetter(PlayerConstraint::count),
				ProgressionPoint.CODEC.fieldOf("point").forGetter(PlayerConstraint::point)
		).apply(i, PlayerConstraint::new));
	}

	private record PlayerCount(float percentage, int left, int killed) {
		private static final float NO_PERCENTAGE = -1.0f;
		private static final int NO_COUNT = -1;

		public static final MapCodec<PlayerCount> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
				Codec.FLOAT.optionalFieldOf("percentage", NO_PERCENTAGE).forGetter(PlayerCount::percentage),
				Codec.INT.optionalFieldOf("left", NO_COUNT).forGetter(PlayerCount::left),
				Codec.INT.optionalFieldOf("killed", NO_COUNT).forGetter(PlayerCount::killed)
		).apply(i, PlayerCount::new));

		public int resolve(int initialCount) {
			int value = 0;
			if (percentage != NO_PERCENTAGE) {
				value = Math.max(value, Mth.floor(percentage * initialCount));
			}
			if (left != NO_COUNT) {
				value = Math.max(value, left);
			}
			if (killed != NO_COUNT) {
				value = Math.max(value, initialCount - killed);
			}
			return value;
		}
	}
}
