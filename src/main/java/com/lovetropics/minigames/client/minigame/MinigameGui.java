package com.lovetropics.minigames.client.minigame;

import com.lovetropics.minigames.Constants;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Constants.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
public class MinigameGui {

	@SubscribeEvent
	public static void renderGameOverlay(RenderGameOverlayEvent event) {
		if (event.getType() != ElementType.TEXT && event.getType() != ElementType.HOTBAR) {
			return;
		}

		MatrixStack transform = event.getMatrixStack();
		FontRenderer fnt = Minecraft.getInstance().fontRenderer;

		final int padding = 2;
		final int lineHeight = fnt.FONT_HEIGHT + padding;
		int y = padding;

		// TODO: reimplement
		/*for (ClientLobbyState state : ClientLobbyState.getLobbies()) {
			// Nothing to show if they are currently playing an active minigame
			GameStatus status = state.getStatus();
			if (status == GameStatus.ACTIVE && state.getRole() != null) return;

			if (event.getType() == ElementType.HOTBAR) {
				Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation("minecraft:missingno"));
			}
			if (event.getType() == ElementType.TEXT) {
				String line = TextFormatting.GRAY + "Minigame: " + TextFormatting.YELLOW + TextFormatting.BOLD + state.getDisplayName();
				if (state.getRole() != null) {
					line += TextFormatting.GREEN + " [Joined]";
				}
				fnt.drawStringWithShadow(transform, line, padding, y, -1);
				y += lineHeight;

				line = (status == GameStatus.POLLING ? TextFormatting.GRAY + "..." : "")
						+ status.color + status.description
						+ " (" + state.getMemberCount(PlayerRole.PARTICIPANT) + "/" + state.getMaxPlayers() + ")";
				fnt.drawStringWithShadow(transform, line, padding, y, -1);
				y += lineHeight;

				line = TextFormatting.GRAY + "Commands: ";
				if (state.getRole() == null) {
					if (state.getStatus() == GameStatus.POLLING) {
						line += TextFormatting.AQUA + "/join" + TextFormatting.GRAY + " or ";
					}
					line += TextFormatting.AQUA + "/spectate";
				} else {
					line += TextFormatting.AQUA + "/leave";
				}
				fnt.drawStringWithShadow(transform, line, padding, y, -1);
				y += lineHeight + padding;
			}
		}*/
	}
}
