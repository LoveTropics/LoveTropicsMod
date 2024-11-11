package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionContext;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionList;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Supplier;

public record ItemPickedUpTrigger(Optional<ItemPredicate> itemPredicate, GameActionList<ServerPlayer> action, boolean consume) implements IGameBehavior {
	public static final MapCodec<ItemPickedUpTrigger> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ItemPredicate.CODEC.optionalFieldOf("item").forGetter(ItemPickedUpTrigger::itemPredicate),
			GameActionList.PLAYER_CODEC.fieldOf("action").forGetter(ItemPickedUpTrigger::action),
			Codec.BOOL.optionalFieldOf("consume", false).forGetter(ItemPickedUpTrigger::consume)
	).apply(i, ItemPickedUpTrigger::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		action.register(game, events);

		events.listen(GamePlayerEvents.PICK_UP_ITEM, (player, item) -> {
			final ItemStack stack = item.getItem();
			if (itemPredicate.isEmpty() || itemPredicate.get().test(stack)) {
				final GameActionContext context = GameActionContext.builder()
						.set(GameActionParameter.ITEM, stack)
						.set(GameActionParameter.COUNT, stack.getCount())
						.build();
				action.apply(game, context, player);
				return consume ? InteractionResult.CONSUME : InteractionResult.PASS;
			}
			return InteractionResult.PASS;
		});
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return GameBehaviorTypes.ITEM_PICKED_UP;
	}
}
