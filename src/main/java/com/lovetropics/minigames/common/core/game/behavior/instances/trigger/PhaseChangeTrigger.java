package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.state.GamePhase;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

public record PhaseChangeTrigger(Map<GamePhase, GameActionList> phases) implements IGameBehavior {
	public static final Codec<PhaseChangeTrigger> CODEC = Codec.unboundedMap(GamePhase.CODEC, GameActionList.CODEC)
			.xmap(PhaseChangeTrigger::new, PhaseChangeTrigger::phases);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		for (GameActionList actions : phases.values()) {
			actions.register(game, events);
		}

		events.listen(GameLogicEvents.PHASE_CHANGE, (phase, lastPhase) -> {
			GameActionList actions = phases.get(phase);
			if (actions != null) {
				actions.apply(GameActionContext.EMPTY, game.getAllPlayers());
			}
		});
	}
}
