package com.lovetropics.minigames.client.minigame;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.minigames.MinigameStatus;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TextFormatting;
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
		ClientMinigameState.get().ifPresent(state -> {
			// Nothing to show if they are currently playing an active minigame
			MinigameStatus status = state.getStatus();
			if (status == MinigameStatus.ACTIVE && state.isJoined()) return;

			if (event.getType() == ElementType.TEXT) {

				MainWindow window = event.getWindow();
				FontRenderer fnt = Minecraft.getInstance().fontRenderer;

				final int padding = 2;
				final int lineHeight = fnt.FONT_HEIGHT + padding;
				int y = window.getScaledHeight() - (lineHeight * 2);

				String line = TextFormatting.GRAY + "Minigame: " + TextFormatting.AQUA + state.getDisplayName();
				fnt.drawStringWithShadow(line, padding, y, -1);
				y += lineHeight;

				line = TextFormatting.GRAY + "..." + status.color + status.description;
				fnt.drawStringWithShadow(line, padding, y, -1);
			}
		});
	}
}
