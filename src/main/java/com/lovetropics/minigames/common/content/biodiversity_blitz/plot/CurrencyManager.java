package com.lovetropics.minigames.common.content.biodiversity_blitz.plot;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public final class CurrencyManager implements IGameState {
	public static final GameStateKey<CurrencyManager> KEY = GameStateKey.create("Currency");

	private final IGamePhase game;

	private final Item item;
	private final Predicate<ItemStack> itemPredicate;

	private final Object2IntMap<UUID> trackedValues = new Object2IntOpenHashMap<>();
	private final Object2IntOpenHashMap<UUID> accumulator = new Object2IntOpenHashMap<>();

	public CurrencyManager(IGamePhase game, Item item) {
		this.game = game;
		this.item = item;
		this.itemPredicate = stack -> stack.getItem() == item;

		this.trackedValues.defaultReturnValue(0);
	}

	public void tickTracked() {
		for (ServerPlayerEntity player : game.getParticipants()) {
			if (player.ticksExisted % 5 == 0) {
				int value = this.get(player);
				this.setTracked(player, value);
			}
		}
	}

	private void setTracked(ServerPlayerEntity player, int value) {
		int lastValue = this.trackedValues.put(player.getUniqueID(), value);
		if (lastValue != value) {
			this.game.invoker(BbEvents.CURRENCY_CHANGED).onCurrencyChanged(player, value, lastValue);
		}
	}

	private void incrementTracked(ServerPlayerEntity player, int amount) {
		int value = this.trackedValues.getInt(player.getUniqueID());
		this.setTracked(player, value + amount);
	}

	public int set(ServerPlayerEntity player, int value) {
		int oldValue = this.get(player);
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

	public int add(ServerPlayerEntity player, int amount) {
		int added = this.addToInventory(player, amount);
		this.incrementTracked(player, added);

		// Accumulate currency
		int lastValue = this.accumulator.getInt(player.getUniqueID());
		this.accumulator.addTo(player.getUniqueID(), added);

		this.game.invoker(BbEvents.CURRENCY_ACCUMULATE).onCurrencyChanged(player, accumulator.getInt(player.getUniqueID()), lastValue);

		return added;
	}

	public int remove(ServerPlayerEntity player, int amount) {
		int removed = this.removeFromInventory(player, amount);
		this.incrementTracked(player, -removed);
		return removed;
	}

	private int addToInventory(ServerPlayerEntity player, int amount) {
		ItemStack stack = new ItemStack(item, amount);
		player.inventory.addItemStackToInventory(stack);
		sendInventoryUpdate(player);
		return amount - stack.getCount();
	}

	private int removeFromInventory(ServerPlayerEntity player, int amount) {
		int remaining = amount;

		List<Slot> slots = player.openContainer.inventorySlots;
		for (Slot slot : slots) {
			remaining -= this.removeFromSlot(slot, remaining);
			if (remaining <= 0) break;
		}

		remaining -= ItemStackHelper.func_233535_a_(player.inventory.getItemStack(), itemPredicate, remaining, false);

		sendInventoryUpdate(player);

		return amount - remaining;
	}

	private int removeFromSlot(Slot slot, int amount) {
		ItemStack stack = slot.getStack();
		if (itemPredicate.test(stack)) {
			int removed = Math.min(amount, stack.getCount());
			stack.shrink(removed);
			return removed;
		}

		return 0;
	}

	public int get(ServerPlayerEntity player) {
		int count = 0;

		List<Slot> slots = player.openContainer.inventorySlots;
		for (Slot slot : slots) {
			ItemStack stack = slot.getStack();
			if (itemPredicate.test(stack)) {
				count += stack.getCount();
			}
		}

		ItemStack stack = player.inventory.getItemStack();
		if (itemPredicate.test(stack)) {
			count += stack.getCount();
		}

		return count;
	}

	private static void sendInventoryUpdate(ServerPlayerEntity player) {
		player.openContainer.detectAndSendChanges();
		player.updateHeldItem();
	}
}
