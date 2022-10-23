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

import java.util.Iterator;
import java.util.List;

public class PhaseControllerBehavior implements IGameBehavior {
	public static final Codec<PhaseControllerBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.validate(PhaseDefinition.CODEC.listOf(), phases -> !phases.isEmpty(), "must give at least one phase")
					.fieldOf("phases").forGetter(c -> c.phases)
	).apply(i, PhaseControllerBehavior::new));

	private GamePhaseState phaseState;

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
		events.listen(GamePhaseEvents.TICK, () -> {
			if (hasFinishedPhases) {
				return;
			}
			PhaseDefinition phase = phases.get(phaseIndex);
			phaseProgress += 1.0 / phase.length();
			if (phaseProgress > 1.0) {
				if (!nextPhase(game)) {
					hasFinishedPhases = true;
				}
			} else {
				phaseState.set(phase.phase(), (float) phaseProgress);
			}
		});
	}

	private record PhaseDefinition(GamePhase phase, int length) {
		public static final Codec<PhaseDefinition> CODEC = RecordCodecBuilder.create(i -> i.group(
				GamePhase.CODEC.fieldOf("key").forGetter(PhaseDefinition::phase),
				Codec.INT.fieldOf("length_in_ticks").forGetter(PhaseDefinition::length)
		).apply(i, PhaseDefinition::new));
	}
}
