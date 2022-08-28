package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.InteractionResult;

public record OnDeathTrigger(GameActionList actions) implements IGameBehavior {
	public static final Codec<OnDeathTrigger> CODEC = RecordCodecBuilder.create(i -> i.group(
		GameActionList.CODEC.fieldOf("actions").forGetter(OnDeathTrigger::actions)
	).apply(i, OnDeathTrigger::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		actions.register(game, events);

		events.listen(GamePlayerEvents.DEATH, (player, damageSource) -> {
			actions.apply(GameActionContext.EMPTY, player);
			return InteractionResult.PASS;
		});
	}
}
