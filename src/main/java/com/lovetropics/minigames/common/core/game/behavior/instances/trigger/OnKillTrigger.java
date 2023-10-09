package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public record OnKillTrigger(GameActionList actions) implements IGameBehavior {
	public static final Codec<OnKillTrigger> CODEC = GameActionList.CODEC.xmap(OnKillTrigger::new, OnKillTrigger::actions);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		actions.register(game, events);

		events.listen(GamePlayerEvents.DEATH, (player, damageSource) -> {
			if (damageSource.getEntity() instanceof ServerPlayer killer) {
				GameActionContext context = GameActionContext.builder()
						.set(GameActionParameter.KILLED, player)
						.set(GameActionParameter.KILLER, killer)
						.build();
				actions.applyPlayer(game, context, killer);
			}
			return InteractionResult.PASS;
		});
	}
}
