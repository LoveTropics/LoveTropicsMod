package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.SidebarClientState;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class GameSidebarRenderer {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final int PADDING = 2;
	private static final int MARGIN = 1;

	@SubscribeEvent
	public static void registerOverlays(RegisterGuiOverlaysEvent event) {
		event.registerBelow(VanillaGuiOverlay.DEBUG_TEXT.id(), "minigame_sidebar", (gui, graphics, partialTick, screenWidth, screenHeight) -> {
			SidebarClientState sidebar = ClientGameStateManager.getOrNull(GameClientStateTypes.SIDEBAR);
			if (sidebar != null) {
				renderSidebar(graphics, sidebar);
			}
		});
	}

	private static void renderSidebar(GuiGraphics graphics, SidebarClientState sidebar) {
		Component title = sidebar.title();
		List<Component> lines = sidebar.lines();

		Font font = CLIENT.font;
		Window window = CLIENT.getWindow();
		Options options = CLIENT.options;
		int screenWidth = window.getGuiScaledWidth();
		int screenHeight = window.getGuiScaledHeight();

		int width = font.width(title);
		for (Component line : lines) {
			width = Math.max(width, font.width(line));
		}

		int right = screenWidth - MARGIN;
		int left = right - width - PADDING;
		int height = (lines.size() + 1) * font.lineHeight + PADDING * 2;
		int top = (screenHeight - height) / 2;
		int bottom = top + height;

		int headerBottom = top + font.lineHeight + PADDING;
		graphics.fill(left, top, right, headerBottom, options.getBackgroundColor(0.4f));
		graphics.fill(left, headerBottom, right, bottom, options.getBackgroundColor(0.3f));

		int textLeft = left + PADDING;
		graphics.drawString(font, title, textLeft, top + PADDING, CommonColors.WHITE);

		int y = headerBottom + 1;
		for (Component line : lines) {
			graphics.drawString(font, line, textLeft, y, CommonColors.WHITE);
			y += font.lineHeight;
		}
	}
}
