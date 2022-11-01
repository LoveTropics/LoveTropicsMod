package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.GamePhase;
import com.lovetropics.minigames.common.core.game.state.GamePhaseState;
import com.lovetropics.minigames.common.util.LinearSpline;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.util.Mth;

import java.util.List;

public class PhaseControllerBehavior implements IGameBehavior {
	public static final Codec<PhaseControllerBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.validate(PhaseDefinition.CODEC.listOf(), phases -> !phases.isEmpty(), "must give at least one phase")
					.fieldOf("phases").forGetter(c -> c.phases),
			Codec.INT.optionalFieldOf("max_time_step", 1).forGetter(b -> b.maxTimeStep),
			PlayerConstraint.CODEC.listOf().optionalFieldOf("time_by_player_count", List.of()).forGetter(b -> b.playerConstraints)
	).apply(i, PhaseControllerBehavior::new));

	private GamePhaseState phaseState;

	private final List<PhaseDefinition> phases;
	private final int maxTimeStep;
	private final List<PlayerConstraint> playerConstraints;

	private Float2FloatFunction playerCountToTime = key -> 0.0f;
	private int currentTime;

	private int phaseIndex;
	private int phaseStartTime;
	private boolean hasFinishedPhases = false;

	public PhaseControllerBehavior(final List<PhaseDefinition> phases, int maxTimeStep, List<PlayerConstraint> playerConstraints) {
		this.phases = phases;
		this.maxTimeStep = maxTimeStep;
		this.playerConstraints = playerConstraints;
	}

	private boolean nextPhase(final IGamePhase game) {
		if (phaseIndex < phases.size() - 1) {
			setPhase(game, phaseIndex + 1);
			return true;
		}
		return false;
	}

	private void setPhase(IGamePhase game, int index) {
		PhaseDefinition lastPhase = phases.get(phaseIndex);
		PhaseDefinition nextPhase = phases.get(index);
		phaseIndex = index;

		if (nextPhase.fixed()) {
			phaseState.set(nextPhase.phase(), nextPhase.length());
		} else {
			phaseState.set(nextPhase.phase());
		}
		phaseStartTime = currentTime;

		if (lastPhase != nextPhase) {
			game.invoker(GameLogicEvents.PHASE_CHANGE).onPhaseChange(nextPhase.phase(), lastPhase.phase());
		}
	}

	@Override
	public void registerState(IGamePhase game, GameStateMap state) {
		phaseState = state.register(GamePhaseState.KEY, new GamePhaseState(phases.get(0).phase(), game.ticks()));
		setPhase(game, 0);
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.START, () -> {
			currentTime = 0;
			playerCountToTime = resolvePlayerConstraints(playerConstraints, game.getParticipants().size());
		});

		events.listen(GamePhaseEvents.TICK, () -> {
			if (hasFinishedPhases) {
				return;
			}
			PhaseDefinition phase = phases.get(phaseIndex);

			int currentTime = tickTime(game, phase);
			int phaseTime = currentTime - phaseStartTime;
			if (phaseTime > phase.length()) {
				if (!nextPhase(game)) {
					hasFinishedPhases = true;
				}
			} else {
				phaseState.update((float) phaseTime / phase.length());
			}
		});
	}

	private int tickTime(IGamePhase game, PhaseDefinition phase) {
		int playerCount = game.getParticipants().size();
		if (phase.fixed()) {
			return ++currentTime;
		}

		int targetTimeByPlayerCount = Mth.ceil(playerCountToTime.get(playerCount));
		if (targetTimeByPlayerCount > currentTime) {
			int step = Math.min(targetTimeByPlayerCount - currentTime, maxTimeStep);
			currentTime += step;
			return currentTime;
		} else {
			return ++currentTime;
		}
	}

	private static Float2FloatFunction resolvePlayerConstraints(List<PlayerConstraint> constraints, int initialPlayerCount) {
		LinearSpline.Builder spline = LinearSpline.builder();
		for (PlayerConstraint constraint : constraints) {
			spline.point(constraint.count().resolve(initialPlayerCount), constraint.time());
		}
		return spline.build();
	}

	private record PhaseDefinition(GamePhase phase, int length, boolean fixed) {
		public static final Codec<PhaseDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
				GamePhase.CODEC.fieldOf("key").forGetter(PhaseDefinition::phase),
				Codec.INT.fieldOf("length_in_ticks").forGetter(PhaseDefinition::length),
				Codec.BOOL.optionalFieldOf("fixed", false).forGetter(PhaseDefinition::fixed)
		).apply(i, PhaseDefinition::new));
	}

	private record PlayerConstraint(PlayerCount count, int time) {
		public static final Codec<PlayerConstraint> CODEC = RecordCodecBuilder.create(i -> i.group(
				PlayerCount.CODEC.forGetter(PlayerConstraint::count),
				Codec.INT.fieldOf("time").forGetter(PlayerConstraint::time)
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
