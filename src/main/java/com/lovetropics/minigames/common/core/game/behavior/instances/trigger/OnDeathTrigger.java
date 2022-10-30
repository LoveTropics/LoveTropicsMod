package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import net.minecraft.world.InteractionResult;

public record OnDeathTrigger(GameActionList actions) implements IGameBehavior {
	public static final Codec<OnDeathTrigger> CODEC = GameActionList.CODEC.xmap(OnDeathTrigger::new, OnDeathTrigger::actions);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		actions.register(game, events);

		events.listen(GamePlayerEvents.DEATH, (player, damageSource) -> {
			actions.apply(game, GameActionContext.EMPTY, player);
			return InteractionResult.PASS;
		});
	}
}
