package com.lovetropics.minigames.common.content.qottott.behavior;

import com.google.gson.JsonSyntaxException;
import com.lovetropics.minigames.common.content.qottott.Qottott;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public record ItemPickupPriorityBehavior(ItemPredicate itemPredicate, float maxSeconds) implements IGameBehavior {
	private static final Codec<ItemPredicate> ITEM_PREDICATE_CODEC = ExtraCodecs.JSON.comapFlatMap(json -> {
		try {
			return DataResult.success(ItemPredicate.fromJson(json));
		} catch (final JsonSyntaxException e) {
			return DataResult.error(e::getMessage);
		}
	}, ItemPredicate::serializeToJson);

	public static final MapCodec<ItemPickupPriorityBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ITEM_PREDICATE_CODEC.optionalFieldOf("item", ItemPredicate.ANY).forGetter(ItemPickupPriorityBehavior::itemPredicate),
			Codec.FLOAT.fieldOf("max_seconds").forGetter(ItemPickupPriorityBehavior::maxSeconds)
	).apply(i, ItemPickupPriorityBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		events.listen(GamePlayerEvents.PICK_UP_ITEM, (player, item) -> {
			final ItemStack stack = item.getItem();
			if (itemPredicate.matches(stack)) {
				final float minSeconds = Math.max(maxSeconds - getPickupPriority(player), 0.0f);
				final int minAge = Mth.floor(minSeconds * SharedConstants.TICKS_PER_SECOND);
				if (item.getAge() <= minAge) {
					return InteractionResult.FAIL;
				}
			}
			return InteractionResult.PASS;
		});
	}

	private static float getPickupPriority(ServerPlayer player) {
		final AttributeInstance attribute = player.getAttribute(Qottott.PICKUP_PRIORITY.get());
		return attribute != null ? (float) attribute.getValue() : 0.0f;
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return Qottott.PICKUP_PRIORITY_BEHAVIOR;
	}
}
