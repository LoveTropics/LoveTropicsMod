package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.GameProgressionState;
import com.lovetropics.minigames.common.core.game.state.ProgressionPoint;
import com.mojang.serialization.Codec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public record PhaseChangeTrigger(Map<ProgressionPoint, GameActionList<Void>> phases) implements IGameBehavior {
	public static final Codec<PhaseChangeTrigger> CODEC = Codec.unboundedMap(ProgressionPoint.CODEC, GameActionList.VOID)
			.xmap(PhaseChangeTrigger::new, PhaseChangeTrigger::phases)
			.fieldOf("phases")
			.codec();

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		GameProgressionState progression = game.getState().getOrThrow(GameProgressionState.KEY);

		for (var actions : phases.values()) {
			actions.register(game, events);
		}

		var remaining = new ArrayList<>(phases.entrySet());

		events.listen(GamePhaseEvents.TICK, () -> {
			var iterator = remaining.iterator();
			while (iterator.hasNext()) {
				var entry = iterator.next();
				if (progression.isAfter(entry.getKey())) {
					entry.getValue().apply(game, GameActionContext.EMPTY);
					iterator.remove();
				}
			}
		});
	}
}
