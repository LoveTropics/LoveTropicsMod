package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

public record ScheduledActionsTrigger(Long2ObjectMap<GameActionList> scheduledActions) implements IGameBehavior {
	public static final Codec<ScheduledActionsTrigger> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.long2Object(GameActionList.CODEC).fieldOf("actions").forGetter(ScheduledActionsTrigger::scheduledActions)
	).apply(i, ScheduledActionsTrigger::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		for (GameActionList actions : scheduledActions.values()) {
			actions.register(game, events);
		}

		events.listen(GamePhaseEvents.TICK, () -> {
			GameActionList actions = scheduledActions.remove(game.ticks());
			if (actions != null) {
				actions.apply(GameActionContext.EMPTY, game.getParticipants());
			}
		});
	}
}
