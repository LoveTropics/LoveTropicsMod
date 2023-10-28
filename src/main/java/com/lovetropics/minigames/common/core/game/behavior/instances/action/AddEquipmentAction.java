package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public record AddEquipmentAction(List<ItemStack> items, ItemStack head, ItemStack chest, ItemStack legs, ItemStack feet, boolean clear) implements IGameBehavior {
	public static final MapCodec<AddEquipmentAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.ITEM_STACK.listOf().optionalFieldOf("items", List.of()).forGetter(AddEquipmentAction::items),
			MoreCodecs.ITEM_STACK.optionalFieldOf("head", ItemStack.EMPTY).forGetter(AddEquipmentAction::head),
			MoreCodecs.ITEM_STACK.optionalFieldOf("chest", ItemStack.EMPTY).forGetter(AddEquipmentAction::chest),
			MoreCodecs.ITEM_STACK.optionalFieldOf("legs", ItemStack.EMPTY).forGetter(AddEquipmentAction::legs),
			MoreCodecs.ITEM_STACK.optionalFieldOf("feet", ItemStack.EMPTY).forGetter(AddEquipmentAction::feet),
			Codec.BOOL.optionalFieldOf("clear", false).forGetter(AddEquipmentAction::clear)
	).apply(i, AddEquipmentAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			if (clear) {
				player.getInventory().clearContent();
			}
			for (final ItemStack item : items) {
				player.getInventory().add(item.copy());
			}
			player.setItemSlot(EquipmentSlot.HEAD, head.copy());
			player.setItemSlot(EquipmentSlot.CHEST, chest.copy());
			player.setItemSlot(EquipmentSlot.LEGS, legs.copy());
			player.setItemSlot(EquipmentSlot.FEET, feet.copy());
			return true;
		});
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return GameBehaviorTypes.ADD_EQUIPMENT;
	}
}
