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
			MoreCodecs.validate(GamePhase.CODEC.listOf(), phases -> !phases.isEmpty(), "must give at least one phase")
					.fieldOf("phases").forGetter(c -> c.phases)
	).apply(i, PhaseControllerBehavior::new));

	private GamePhaseState phaseState;

	private final List<GamePhase> phases;
	private Iterator<GamePhase> phaseIterator;
	private int currentPhaseTicks;
	private boolean hasFinishedPhases = false;

	public PhaseControllerBehavior(final List<GamePhase> phases) {
		this.phases = phases;
	}

	private boolean nextPhase(final IGamePhase game) {
		if (phaseIterator.hasNext()) {
			GamePhase lastPhase = phaseState.get();

			GamePhase nextPhase = phaseIterator.next();
			phaseState.set(nextPhase);
			currentPhaseTicks = nextPhase.lengthInTicks();

			if (lastPhase != nextPhase) {
				game.invoker(GameLogicEvents.PHASE_CHANGE).onPhaseChange(nextPhase, lastPhase);
			}

			return true;
		}

		return false;
	}

	@Override
	public void registerState(IGamePhase game, GameStateMap state) {
		phaseState = state.register(GamePhaseState.KEY, new GamePhaseState(phases.get(0)));
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.START, () -> {
			hasFinishedPhases = false;
			phaseIterator = phases.iterator();
			nextPhase(game);
		});

		events.listen(GamePhaseEvents.TICK, () -> {
			if (!hasFinishedPhases && currentPhaseTicks-- < 0) {
				if (!nextPhase(game)) {
					hasFinishedPhases = true;
				}
			}
		});
	}
}
