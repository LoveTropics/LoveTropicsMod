package com.lovetropics.minigames.client.minigame;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.minigames.MinigameStatus;
import com.lovetropics.minigames.common.minigames.PlayerRole;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Constants.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
public class MinigameGui {

	@SubscribeEvent
	public static void renderGameOverlay(RenderGameOverlayEvent event) {
		ClientMinigameState.get().ifPresent(state -> {
			// Nothing to show if they are currently playing an active minigame
			MinigameStatus status = state.getStatus();
			if (status == MinigameStatus.ACTIVE && state.getRole() != null) return;

			if (event.getType() == ElementType.HOTBAR) {
				Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation("minecraft:missingno"));
			}
			if (event.getType() == ElementType.TEXT) {
				FontRenderer fnt = Minecraft.getInstance().fontRenderer;

				final int padding = 2;
				final int lineHeight = fnt.FONT_HEIGHT + padding;
				int y = padding;

				String line = TextFormatting.GRAY + "Minigame: " + TextFormatting.YELLOW + TextFormatting.BOLD + state.getDisplayName();
				if (state.getRole() != null) {
					line += TextFormatting.GREEN + " [Joined]";
				}
				fnt.drawStringWithShadow(line, padding, y, -1);
				y += lineHeight;

				line = (status == MinigameStatus.POLLING ? TextFormatting.GRAY + "..." : "")
						+ status.color + status.description
						+ " (" + state.getMemberCount(PlayerRole.PARTICIPANT) + "/" + state.getMaxPlayers() + ")";
				fnt.drawStringWithShadow(line, padding, y, -1);
				y += lineHeight;

				line = TextFormatting.GRAY + "Commands: ";
				if (state.getRole() == null) {
					line += TextFormatting.AQUA + "/join" + TextFormatting.GRAY + " or " + TextFormatting.AQUA + "/spectate";
				} else {
					line += TextFormatting.AQUA + "/leave";
				}
				fnt.drawStringWithShadow(line, padding, y, -1);
			}
		});
	}

	@SubscribeEvent
	public static void onClientDisconnect(LoggedOutEvent event) {
		ClientMinigameState.set(null);
	}
}
