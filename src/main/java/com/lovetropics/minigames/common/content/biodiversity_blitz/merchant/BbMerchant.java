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
    public void setTradingPlayer(@Nullable PlayerEntity player) {

    }

    @Nullable
    @Override
    public PlayerEntity getTradingPlayer() {
        return this.customer;
    }

    @Override
    public MerchantOffers getOffers() {
        return this.offers;
    }

    @Override
    public void overrideOffers(@Nullable MerchantOffers offers) {

    }

    @Override
    public void notifyTrade(MerchantOffer offer) {

    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {

    }

    @Override
    public World getLevel() {
        return this.customer.level;
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public void overrideXp(int xpIn) {

    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public void openTradingScreen(PlayerEntity player, ITextComponent displayName, int level) {
        OptionalInt container = player.openMenu(new SimpleNamedContainerProvider(this::createContainer, displayName));
        if (container.isPresent()) {
            MerchantOffers offers = this.getOffers();
            if (!offers.isEmpty()) {
                player.sendMerchantOffers(container.getAsInt(), offers, level, this.getVillagerXp(), this.showProgressBar(), this.canRestock());
            }
        }
    }

    private MerchantContainer createContainer(int id, PlayerInventory playerInventory, PlayerEntity player) {
        return new BbMerchantContainer(this, id, playerInventory);
    }
}
