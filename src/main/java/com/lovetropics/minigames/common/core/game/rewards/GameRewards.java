package com.lovetropics.minigames.common.core.game.rewards;

import com.lovetropics.minigames.common.content.MinigameTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GameRewards {
	private final List<ItemStack> stacks = new ArrayList<>();

	public void give(final ItemStack item) {
		final ItemStack remainder = tryMergeIntoExistingStack(item.copy());
		if (!remainder.isEmpty()) {
			stacks.add(remainder);
		}
	}

	private ItemStack tryMergeIntoExistingStack(final ItemStack item) {
		for (final ItemStack stack : stacks) {
			if (!ItemStack.isSameItemSameTags(item, stack)) {
				continue;
			}
			final int maxAmount = stack.getMaxStackSize() - stack.getCount();
			final int amount = Math.min(item.getCount(), maxAmount);
			if (amount > 0) {
				stack.grow(amount);
				item.shrink(amount);
				if (item.isEmpty()) {
					return ItemStack.EMPTY;
				}
			}
		}
		return item;
	}

	public void grant(final ServerPlayer player) {
		if (stacks.isEmpty()) {
			return;
		}
		player.sendSystemMessage(MinigameTexts.REWARDS);
		for (final ItemStack item : stacks) {
			player.sendSystemMessage(MinigameTexts.REWARD_ITEM.apply(
					Component.literal(String.valueOf(item.getCount())),
					item.getDisplayName().copy().withStyle(ChatFormatting.AQUA)
			));
			player.getInventory().placeItemBackInInventory(item);
		}
	}
}
