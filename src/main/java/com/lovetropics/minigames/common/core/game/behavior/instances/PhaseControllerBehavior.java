package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.instances.GamePhase;
import com.lovetropics.minigames.common.core.game.state.instances.GamePhaseState;
import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Iterator;
import java.util.List;

public class PhaseControllerBehavior implements IGameBehavior {
	public static final Codec<PhaseControllerBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.validate(GamePhase.CODEC.listOf(), phases -> !phases.isEmpty(), "must give at least one phase")
						.fieldOf("phases").forGetter(c -> c.phases)
		).apply(instance, PhaseControllerBehavior::new);
	});

	private GamePhaseState phaseState;

	private final List<GamePhase> phases;
	private Iterator<GamePhase> phaseIterator;
	private int currentPhaseTicks;
	private boolean hasFinishedPhases = false;

	public PhaseControllerBehavior(final List<GamePhase> phases) {
		this.phases = phases;
	}

	private boolean nextPhase(final IActiveGame game) {
		if (phaseIterator.hasNext()) {
			GamePhase lastPhase = phaseState.get();

			GamePhase nextPhase = phaseIterator.next();
			phaseState.set(nextPhase);
			currentPhaseTicks = nextPhase.lengthInTicks;

			if (lastPhase != nextPhase) {
				game.invoker(GameLogicEvents.PHASE_CHANGE).onPhaseChange(game, nextPhase, lastPhase);
			}

			return true;
		}

		return false;
	}

	@Override
	public void registerState(GameStateMap state) {
		phaseState = state.register(GamePhaseState.TYPE, new GamePhaseState(phases.get(0)));
	}

	@Override
	public void register(IActiveGame registerGame, EventRegistrar events) {
		events.listen(GameLifecycleEvents.START, game -> {
			hasFinishedPhases = false;
			phaseIterator = phases.iterator();
			nextPhase(game);
		});

		events.listen(GameLifecycleEvents.TICK, game -> {
			if (!hasFinishedPhases && currentPhaseTicks-- < 0) {
				if (!nextPhase(game)) {
					hasFinishedPhases = true;
				}
			}
		});
	}
}
