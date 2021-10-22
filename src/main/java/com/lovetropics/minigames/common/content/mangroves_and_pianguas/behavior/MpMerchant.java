package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior;

import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.OptionalInt;

public final class MpMerchant implements IMerchant {
    private final PlayerEntity customer;
    private final MerchantOffers offers;

    public MpMerchant(PlayerEntity player, MerchantOffers offers) {
        this.customer = player;
        this.offers = offers;
    }

    @Override
    public void setCustomer(@Nullable PlayerEntity player) {

    }

    @Nullable
    @Override
    public PlayerEntity getCustomer() {
        return this.customer;
    }

    @Override
    public MerchantOffers getOffers() {
        return this.offers;
    }

    @Override
    public void setClientSideOffers(@Nullable MerchantOffers offers) {

    }

    @Override
    public void onTrade(MerchantOffer offer) {

    }

    @Override
    public void verifySellingItem(ItemStack stack) {

    }

    @Override
    public World getWorld() {
        return this.customer.world;
    }

    @Override
    public int getXp() {
        return 0;
    }

    @Override
    public void setXP(int xpIn) {

    }

    @Override
    public boolean hasXPBar() {
        return false;
    }

    @Override
    public SoundEvent getYesSound() {
        return SoundEvents.ENTITY_VILLAGER_YES;
    }

    @Override
    public void openMerchantContainer(PlayerEntity player, ITextComponent displayName, int level) {
        OptionalInt container = player.openContainer(new SimpleNamedContainerProvider(this::createContainer, displayName));
        if (container.isPresent()) {
            MerchantOffers offers = this.getOffers();
            if (!offers.isEmpty()) {
                player.openMerchantContainer(container.getAsInt(), offers, level, this.getXp(), this.hasXPBar(), this.canRestockTrades());
            }
        }
    }

    private MerchantContainer createContainer(int id, PlayerInventory playerInventory, PlayerEntity player) {
        return new MerchantContainer(id, playerInventory, this) {
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
        };
    }
}
