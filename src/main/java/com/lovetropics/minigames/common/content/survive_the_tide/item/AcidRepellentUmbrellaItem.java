package com.lovetropics.minigames.common.content.survive_the_tide.item;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.content.survive_the_tide.SurviveTheTide;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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
        super(properties.durability(180));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("Prevents acid rain from harming you.").withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("Active when held in main hand or off-hand.").withStyle(ChatFormatting.AQUA));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }

        Player player = event.player;
        if (player.getDeltaMovement().y() < 0.0 && !player.getAbilities().flying && isHoldingItem(player, SurviveTheTide.ACID_REPELLENT_UMBRELLA.get())) {
            player.setDeltaMovement(player.getDeltaMovement().multiply(1.0, 0.8, 1.0));
            player.fallDistance = 0.0F;
        }
    }

    private static boolean isHoldingItem(Player player, Item item) {
        return player.getMainHandItem().getItem() == item || player.getOffhandItem().getItem() == item;
    }
}
