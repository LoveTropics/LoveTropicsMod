package com.lovetropics.minigames.common.core.game.behavior.instances.trigger;

import com.google.gson.JsonSyntaxException;
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
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public record ItemPickedUpTrigger(ItemPredicate itemPredicate, GameActionList<ServerPlayer> action, boolean consume) implements IGameBehavior {
	private static final Codec<ItemPredicate> ITEM_PREDICATE_CODEC = ExtraCodecs.JSON.comapFlatMap(json -> {
		try {
			return DataResult.success(ItemPredicate.fromJson(json));
		} catch (final JsonSyntaxException e) {
			return DataResult.error(e::getMessage);
		}
	}, ItemPredicate::serializeToJson);

	public static final MapCodec<ItemPickedUpTrigger> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ITEM_PREDICATE_CODEC.optionalFieldOf("item", ItemPredicate.ANY).forGetter(ItemPickedUpTrigger::itemPredicate),
			GameActionList.PLAYER_CODEC.fieldOf("action").forGetter(ItemPickedUpTrigger::action),
			Codec.BOOL.optionalFieldOf("consume", false).forGetter(ItemPickedUpTrigger::consume)
	).apply(i, ItemPickedUpTrigger::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		action.register(game, events);

		events.listen(GamePlayerEvents.PICK_UP_ITEM, (player, item) -> {
			final ItemStack stack = item.getItem();
			if (itemPredicate.matches(stack)) {
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
