package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.action.PlayerActionTarget;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

// TODO ideally this would use a void target.. but we have too many uses using a player
public record ScheduledActionsTrigger(PlayerActionTarget target, Long2ObjectMap<GameActionList<ServerPlayer>> scheduledActions) implements IGameBehavior {
	public static final MapCodec<ScheduledActionsTrigger> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			PlayerActionTarget.Target.CODEC.xmap(PlayerActionTarget::new, PlayerActionTarget::target).optionalFieldOf("target", PlayerActionTarget.SOURCE).forGetter(ScheduledActionsTrigger::target),
			MoreCodecs.long2Object(GameActionList.PLAYER_CODEC).fieldOf("actions").forGetter(ScheduledActionsTrigger::scheduledActions)
	).apply(i, ScheduledActionsTrigger::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		for (GameActionList<ServerPlayer> actions : scheduledActions.values()) {
			actions.register(game, events);
		}

		events.listen(GamePhaseEvents.TICK, () -> {
			GameActionList<ServerPlayer> actions = scheduledActions.remove(game.ticks());
			if (actions != null) {
				actions.apply(game, GameActionContext.EMPTY, target.resolve(game, List.of()));
			}
		});
	}
}
