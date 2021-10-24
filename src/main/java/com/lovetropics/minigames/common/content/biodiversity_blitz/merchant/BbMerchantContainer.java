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
	public ItemStack transferStackInSlot(PlayerEntity player, int index) {
		// copied from MerchantContainer.transferStackInSlot removing a problematic cast

		ItemStack resultStack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack()) {
			ItemStack slotStack = slot.getStack();
			resultStack = slotStack.copy();

			if (index == 2) {
				if (!this.mergeItemStack(slotStack, 3, 39, true)) {
					return ItemStack.EMPTY;
				}

				slot.onSlotChange(slotStack, resultStack);
			} else if (index != 0 && index != 1) {
				if (index >= 3 && index < 30) {
					if (!this.mergeItemStack(slotStack, 30, 39, false)) {
						return ItemStack.EMPTY;
					}
				} else if (index >= 30 && index < 39 && !this.mergeItemStack(slotStack, 3, 30, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(slotStack, 3, 39, false)) {
				return ItemStack.EMPTY;
			}

			if (slotStack.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (slotStack.getCount() == resultStack.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, slotStack);
		}

		return resultStack;
	}
}
