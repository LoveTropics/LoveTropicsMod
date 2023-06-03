package com.lovetropics.minigames.common.content.survive_the_tide.item;

import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class SuperSunscreenItem extends Item {
    public SuperSunscreenItem(Properties properties) {
        super(properties.durability(180));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Prevents heatwaves from slowing you down.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Active when held in main hand or off-hand.").withStyle(ChatFormatting.AQUA));
    }
}
