package com.lovetropics.minigames.common.item.minigame;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class AcidRepellentUmbrellaItem extends Item {
    public AcidRepellentUmbrellaItem(Properties properties) {
        super(properties.maxDamage(180));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("Prevents acid rain from harming you.").applyTextStyle(TextFormatting.GOLD));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Active when held in main hand or off-hand.").applyTextStyle(TextFormatting.AQUA));
    }
}
