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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

import java.util.List;

public class PhaseControllerBehavior implements IGameBehavior {
	public static final Codec<PhaseControllerBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.validate(PhaseDefinition.CODEC.listOf(), phases -> !phases.isEmpty(), "must give at least one phase")
					.fieldOf("phases").forGetter(c -> c.phases)
	).apply(i, PhaseControllerBehavior::new));

	private GamePhaseState phaseState;
	private int initialPlayerCount;

	private final List<PhaseDefinition> phases;
	private int phaseIndex;
	private double phaseProgress;
	private boolean hasFinishedPhases = false;

	public PhaseControllerBehavior(final List<PhaseDefinition> phases) {
		this.phases = phases;
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

		phaseState.set(nextPhase.phase(), 0.0f);
		phaseProgress = 0;

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
		initialPlayerCount = game.getParticipants().size();

		events.listen(GamePhaseEvents.TICK, () -> {
			if (hasFinishedPhases) {
				return;
			}
			PhaseDefinition phase = phases.get(phaseIndex);

			boolean advancing = phase.advanceConditions().test(game, initialPlayerCount);
			double speedFactor = advancing ? phase.advanceSpeedFactor() : 1.0;
			phaseProgress += speedFactor / phase.length();

			if (phaseProgress > 1.0) {
				if (!nextPhase(game)) {
					hasFinishedPhases = true;
				}
			} else {
				phaseState.set(phase.phase(), (float) phaseProgress);
			}
		});
	}

	private record PhaseDefinition(GamePhase phase, int length, float advanceSpeedFactor, AdvanceConditions advanceConditions) {
		public static final Codec<PhaseDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
				GamePhase.CODEC.fieldOf("key").forGetter(PhaseDefinition::phase),
				Codec.INT.fieldOf("length_in_ticks").forGetter(PhaseDefinition::length),
				Codec.FLOAT.optionalFieldOf("advance_speed_factor", 4.0f).forGetter(PhaseDefinition::advanceSpeedFactor),
				AdvanceConditions.CODEC.optionalFieldOf("advance_conditions", AdvanceConditions.DEFAULT).forGetter(PhaseDefinition::advanceConditions)
		).apply(i, PhaseDefinition::new));
	}

	private record AdvanceConditions(
			float belowPlayerPercentage,
			int belowPlayerCount
	) {
		public static final Codec<AdvanceConditions> CODEC = RecordCodecBuilder.create(i -> i.group(
				Codec.FLOAT.optionalFieldOf("below_player_percentage", -1.0f).forGetter(AdvanceConditions::belowPlayerPercentage),
				Codec.INT.optionalFieldOf("below_player_count", -1).forGetter(AdvanceConditions::belowPlayerCount)
		).apply(i, AdvanceConditions::new));

		public static final AdvanceConditions DEFAULT = new AdvanceConditions(-1.0f, -1);

		public boolean test(IGamePhase game, int initialPlayerCount) {
			int playerCount = game.getParticipants().size();
			float playerPercentage = (float) playerCount / initialPlayerCount;
			return playerPercentage <= belowPlayerPercentage || playerCount <= belowPlayerCount;
		}
	}
}
