package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.ActionTarget;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.action.PlayerActionTarget;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;

import java.util.List;

public record ScheduledActionsTrigger<T, A extends ActionTarget<T>>(A target, Long2ObjectMap<GameActionList<?, ?>> scheduledActions) implements IGameBehavior {
	public static final Codec<ScheduledActionsTrigger<?, ?>> CODEC = RecordCodecBuilder.create(i -> i.group(
			ActionTarget.FALLBACK_PLAYER.optionalFieldOf("target", PlayerActionTarget.SOURCE).forGetter(ScheduledActionsTrigger::target),
			MoreCodecs.long2Object(GameActionList.TYPE_SAFE_CODEC).fieldOf("actions").forGetter(ScheduledActionsTrigger::scheduledActions)
	).apply(i, ScheduledActionsTrigger::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		for (GameActionList<?, ?> actions : scheduledActions.values()) {
			actions.register(game, events);
		}

		events.listen(GamePhaseEvents.TICK, () -> {
			GameActionList<?, ?> actions = scheduledActions.remove(game.ticks());
			if (actions != null) {
				if (actions.target.type() == target.type()) {
					actions.applyIf(target::type, game, GameActionContext.EMPTY, target.resolve(game, List.of()));
				} else {
					actions.apply(game, GameActionContext.EMPTY);
				}
			}
		});
	}
}
