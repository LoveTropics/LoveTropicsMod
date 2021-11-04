package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public final class SelectorItems<V> {
	private static final String KEY = "selector_key";

	private final Handlers<V> handlers;
	private final V[] values;

	public SelectorItems(Handlers<V> handlers, V[] values) {
		this.handlers = handlers;
		this.values = values;
	}

	public void applyTo(EventRegistrar events) {
		events.listen(GamePlayerEvents.USE_ITEM, this::onUseItem);
		events.listen(GamePlayerEvents.THROW_ITEM, this::onThrowItem);
	}

	public void giveSelectorsTo(ServerPlayerEntity player) {
		for (V value : this.values) {
			IItemProvider item = handlers.getItemFor(value);
			player.addItemStackToInventory(this.createSelectorItem(item, value));
		}
	}

	private ActionResultType onUseItem(ServerPlayerEntity player, Hand hand) {
		ItemStack heldStack = player.getHeldItem(hand);
		if (heldStack.isEmpty()) {
			return ActionResultType.PASS;
		}

		V value = getValueForSelector(heldStack);
		if (value != null) {
			this.handlers.onPlayerSelected(player, value);
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	private ActionResultType onThrowItem(ServerPlayerEntity player, ItemEntity entity) {
		V value = getValueForSelector(entity.getItem());
		if (value != null) {
			return ActionResultType.FAIL;
		}

		return ActionResultType.PASS;
	}

	@Nullable
	private V getValueForSelector(ItemStack stack) {
		CompoundNBT tag = stack.getTag();
		if (tag == null) return null;

		String id = tag.getString(KEY);
		for (V value : this.values) {
			if (this.handlers.getIdFor(value).equals(id)) {
				return value;
			}
		}

		return null;
	}

	private ItemStack createSelectorItem(IItemProvider item, V value) {
		ItemStack stack = new ItemStack(item);
		stack.setDisplayName(this.handlers.getNameFor(value));

		CompoundNBT tag = stack.getOrCreateTag();
		tag.putString(KEY, this.handlers.getIdFor(value));

		return stack;
	}

	public interface Handlers<V> {
		void onPlayerSelected(ServerPlayerEntity player, V value);

		String getIdFor(V value);

		ITextComponent getNameFor(V value);

		IItemProvider getItemFor(V value);
	}
}
