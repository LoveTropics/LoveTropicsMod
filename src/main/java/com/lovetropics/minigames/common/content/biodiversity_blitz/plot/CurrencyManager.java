package com.lovetropics.minigames.common.content.biodiversity_blitz.plot;

import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class CurrencyManager {
	private static final Supplier<Item> ITEM = BiodiversityBlitz.OSA_POINT;
	private static final Predicate<ItemStack> CURRENCY_PREDICATE = stack -> stack.getItem() == ITEM.get();

	public static int set(ServerPlayerEntity player, int value) {
		int oldValue = get(player);
		if (value > oldValue) {
			int increment = value - oldValue;
			return oldValue + add(player, increment);
		} else if (value < oldValue) {
			int decrement = oldValue - value;
			return oldValue - remove(player, decrement);
		} else {
			return value;
		}
	}

	public static int add(ServerPlayerEntity player, int amount) {
		ItemStack stack = new ItemStack(ITEM.get(), amount);
		player.inventory.addItemStackToInventory(stack);
		sendInventoryUpdate(player);

		return amount - stack.getCount();
	}

	public static int remove(ServerPlayerEntity player, int amount) {
		List<Slot> slots = player.openContainer.inventorySlots;
		for (Slot slot : slots) {
			amount -= removeFromSlot(slot, amount);
			if (amount <= 0) break;
		}

		amount -= ItemStackHelper.func_233535_a_(player.inventory.getItemStack(), CURRENCY_PREDICATE, amount, false);

		sendInventoryUpdate(player);

		return amount;
	}

	private static int removeFromSlot(Slot slot, int amount) {
		ItemStack stack = slot.getStack();
		if (CURRENCY_PREDICATE.test(stack)) {
			int removed = Math.min(amount, stack.getCount());
			stack.shrink(removed);
			return removed;
		}

		return 0;
	}

	public static int get(ServerPlayerEntity player) {
		int count = 0;

		List<Slot> slots = player.openContainer.inventorySlots;
		for (Slot slot : slots) {
			ItemStack stack = slot.getStack();
			if (CURRENCY_PREDICATE.test(stack)) {
				count += stack.getCount();
			}
		}

		ItemStack stack = player.inventory.getItemStack();
		if (CURRENCY_PREDICATE.test(stack)) {
			count += stack.getCount();
		}

		return count;
	}

	private static void sendInventoryUpdate(ServerPlayerEntity player) {
		player.openContainer.detectAndSendChanges();
		player.updateHeldItem();
	}
}
