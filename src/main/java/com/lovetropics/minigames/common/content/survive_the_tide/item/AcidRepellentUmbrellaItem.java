package com.lovetropics.minigames.common.content.survive_the_tide.item;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.content.survive_the_tide.SurviveTheTide;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public class AcidRepellentUmbrellaItem extends Item {
    public AcidRepellentUmbrellaItem(Properties properties) {
        super(properties.maxDamage(180));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new StringTextComponent("Prevents acid rain from harming you.").mergeStyle(TextFormatting.GOLD));
        tooltip.add(new StringTextComponent(""));
        tooltip.add(new StringTextComponent("Active when held in main hand or off-hand.").mergeStyle(TextFormatting.AQUA));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }

        PlayerEntity player = event.player;
        if (player.getMotion().getY() < 0.0 && !player.abilities.isFlying && isHoldingItem(player, SurviveTheTide.ACID_REPELLENT_UMBRELLA.get())) {
            player.setMotion(player.getMotion().mul(1.0, 0.8, 1.0));
            player.fallDistance = 0.0F;
        }
    }

    private static boolean isHoldingItem(PlayerEntity player, Item item) {
        return player.getHeldItemMainhand().getItem() == item || player.getHeldItemOffhand().getItem() == item;
    }
}
