package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behaviors;

import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.MerchantInventory;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.tropicraft.core.common.block.TropicraftBlocks;

import javax.annotation.Nullable;

public final class MpMerchant implements IMerchant {
    private static final ResourceLocation IRIS = new ResourceLocation("tropicraft", "iris");
    private static final MerchantOffers OFFERS = new MerchantOffers();
    static {
        populateOffers();
    }

    private final PlayerEntity customer;

    public MpMerchant(PlayerEntity player) {
        this.customer = player;
    }

    private static void populateOffers() {
        OFFERS.add(offer(4, Items.SWEET_BERRIES));
        OFFERS.add(offer(1, Items.POTATO));
        OFFERS.add(offer(1, Items.CARROT));
        OFFERS.add(offer(1, new ItemStack(Items.WHEAT_SEEDS, 2)));
        OFFERS.add(offer(2, Items.GRASS));
        OFFERS.add(offer(32, Items.WITHER_ROSE));
        OFFERS.add(offer(12, Items.MELON));
        if (Registry.ITEM.containsKey(IRIS)) {
            OFFERS.add(offer(12, Registry.ITEM.getOrDefault(IRIS)));
        }
        OFFERS.add(offer(32, Items.JACK_O_LANTERN));
        OFFERS.add(offer(24, Items.BIRCH_SAPLING));
        OFFERS.add(offer(12, Items.PUMPKIN));
        OFFERS.add(offer(12, Items.OAK_BOAT));
        OFFERS.add(offer(32, Items.IRON_SWORD));
        OFFERS.add(offer(64, Items.IRON_AXE));
        OFFERS.add(offer(64, Items.BOW));
        OFFERS.add(offer(16, new ItemStack(Items.ARROW, 4)));
    }

    private static MerchantOffer offer(int price, Item out) {
        return offer(price, new ItemStack(out));
    }

    private static MerchantOffer offer(int price, ItemStack out) {
        return new MerchantOffer(
                new ItemStack(Items.SUNFLOWER, price),
                out,
                Integer.MAX_VALUE,
                0, 0
        );
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
        return OFFERS;
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
}
