package com.lovetropics.minigames.common.content.survive_the_tide.item;

import com.lovetropics.minigames.common.content.survive_the_tide.SurviveTheTideTexts;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class SuperSunscreenItem extends Item {
    public SuperSunscreenItem(Properties properties) {
        super(properties.durability(180));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(SurviveTheTideTexts.SUPER_SUNSCREEN_TOOLTIP);
    }
}
