package com.lovetropics.minigames.common;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.item.MinigameItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class UmbrellaParachute {
	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			return;
		}

		PlayerEntity player = event.player;
		if (player.getMotion().getY() < 0.0 && !player.abilities.isFlying && isHoldingItem(player, MinigameItems.ACID_REPELLENT_UMBRELLA.get())) {
			player.setMotion(player.getMotion().mul(1.0, 0.8, 1.0));
			player.fallDistance = 0.0F;
		}
	}

	private static boolean isHoldingItem(PlayerEntity player, Item item) {
		return player.getHeldItemMainhand().getItem() == item || player.getHeldItemOffhand().getItem() == item;
	}
}
