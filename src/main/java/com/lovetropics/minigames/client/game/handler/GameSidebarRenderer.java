package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.game.ClientGameStateManager;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.lovetropics.minigames.common.core.game.client_state.instance.SidebarClientState;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MODID, value = Dist.CLIENT)
public final class GameSidebarRenderer {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	private static final int PADDING = 2;
	private static final int MARGIN = 1;

	@SubscribeEvent
	public static void renderGameOverlay(RenderGameOverlayEvent event) {
		if (event.getType() != RenderGameOverlayEvent.ElementType.TEXT) {
			return;
		}

		SidebarClientState sidebar = ClientGameStateManager.getOrNull(GameClientStateTypes.SIDEBAR);
		if (sidebar != null) {
			renderSidebar(event, sidebar);
		}
	}

	private static void renderSidebar(RenderGameOverlayEvent event, SidebarClientState sidebar) {
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

		PoseStack poseStack = event.getMatrixStack();

		int headerBottom = top + font.lineHeight + PADDING;
		GuiComponent.fill(poseStack, left, top, right, headerBottom, options.getBackgroundColor(0.4f));
		GuiComponent.fill(poseStack, left, headerBottom, right, bottom, options.getBackgroundColor(0.3f));

		int textLeft = left + PADDING;
		font.draw(poseStack, title, textLeft, top + PADDING, 0xffffffff);

		int y = headerBottom + 1;
		for (Component line : lines) {
			font.draw(poseStack, line, textLeft, y, 0xffffffff);
			y += font.lineHeight;
		}
	}
}
