package com.lovetropics.minigames.common.content.biodiversity_blitz.merchant;

import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.OptionalInt;

public final class BbMerchant implements IMerchant {
    private final PlayerEntity customer;
    private final MerchantOffers offers;

    public BbMerchant(PlayerEntity player, MerchantOffers offers) {
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
        return new BbMerchantContainer(this, id, playerInventory);
    }
}
