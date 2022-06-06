package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.ItemLike;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public record SelectorItems<V>(Handlers<V> handlers, V[] values) {
	private static final String KEY = "selector_key";

	public void applyTo(EventRegistrar events) {
		events.listen(GamePlayerEvents.USE_ITEM, this::onUseItem);
		events.listen(GamePlayerEvents.THROW_ITEM, this::onThrowItem);
	}

	public void giveSelectorsTo(ServerPlayer player) {
		for (V value : this.values) {
			ItemLike item = handlers.getItemFor(value);
			player.addItem(this.createSelectorItem(item, value));
		}
	}

	private InteractionResult onUseItem(ServerPlayer player, InteractionHand hand) {
		ItemStack heldStack = player.getItemInHand(hand);
		if (heldStack.isEmpty()) {
			return InteractionResult.PASS;
		}

		V value = getValueForSelector(heldStack);
		if (value != null) {
			this.handlers.onPlayerSelected(player, value);
			return InteractionResult.SUCCESS;
		}

		return InteractionResult.PASS;
	}

	private InteractionResult onThrowItem(ServerPlayer player, ItemEntity entity) {
		V value = getValueForSelector(entity.getItem());
		if (value != null) {
			return InteractionResult.FAIL;
		}

		return InteractionResult.PASS;
	}

	@Nullable
	private V getValueForSelector(ItemStack stack) {
		CompoundTag tag = stack.getTag();
		if (tag == null) return null;

		String id = tag.getString(KEY);
		for (V value : this.values) {
			if (this.handlers.getIdFor(value).equals(id)) {
				return value;
			}
		}

		return null;
	}

	private ItemStack createSelectorItem(ItemLike item, V value) {
		ItemStack stack = new ItemStack(item);
		stack.setHoverName(this.handlers.getNameFor(value));

		CompoundTag tag = stack.getOrCreateTag();
		tag.putString(KEY, this.handlers.getIdFor(value));

		return stack;
	}

	public interface Handlers<V> {
		void onPlayerSelected(ServerPlayer player, V value);

		String getIdFor(V value);

		Component getNameFor(V value);

		ItemLike getItemFor(V value);
	}
}
