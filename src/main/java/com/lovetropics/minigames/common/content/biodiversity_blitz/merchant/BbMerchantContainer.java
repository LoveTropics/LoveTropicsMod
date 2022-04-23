package com.lovetropics.minigames.common.content.biodiversity_blitz.merchant;

import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public final class BbMerchantContainer extends MerchantContainer {
	public BbMerchantContainer(IMerchant merchant, int id, PlayerInventory playerInventory) {
		super(id, playerInventory, merchant);
	}

	@Override
	public ItemStack quickMoveStack(PlayerEntity player, int index) {
		// copied from MerchantContainer.transferStackInSlot removing a problematic cast

		ItemStack resultStack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem()) {
			ItemStack slotStack = slot.getItem();
			resultStack = slotStack.copy();

			if (index == 2) {
				if (!this.moveItemStackTo(slotStack, 3, 39, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(slotStack, resultStack);
			} else if (index != 0 && index != 1) {
				if (index >= 3 && index < 30) {
					if (!this.moveItemStackTo(slotStack, 30, 39, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= 30 && index < 39 && !this.moveItemStackTo(slotStack, 3, 30, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.moveItemStackTo(slotStack, 3, 39, false)) {
				return ItemStack.EMPTY;
			}

			if (slotStack.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (slotStack.getCount() == resultStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, slotStack);
		}

		return resultStack;
	}
}
