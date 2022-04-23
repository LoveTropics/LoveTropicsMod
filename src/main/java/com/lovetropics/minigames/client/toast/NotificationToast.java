package com.lovetropics.minigames.client.toast;

import com.lovetropics.minigames.Constants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.components.toasts.Toast.Visibility;

public final class NotificationToast implements Toast {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MODID, "textures/gui/toasts.png");

	private static final int TEXTURE_WIDTH = 160;
	private static final int TEXTURE_HEIGHT = 32;
	private static final int TEXTURE_BORDER = 4;

	private static final int ICON_SIZE = 18;
	private static final int TEXT_LEFT = ICON_SIZE + 8;
	private static final int MAX_WIDTH = 160 - TEXT_LEFT;

	private static final int LINE_HEIGHT = 12;

	private static final Minecraft CLIENT = Minecraft.getInstance();

	private final List<FormattedCharSequence> lines;
	private final NotificationDisplay display;

	private final int width;
	private final int height;

	public NotificationToast(Component message, NotificationDisplay display) {
		Font fontRenderer = CLIENT.font;

		List<FormattedCharSequence> lines = new ArrayList<>(2);
		lines.addAll(fontRenderer.split(message, MAX_WIDTH));

		int textWidth = Math.max(lines.stream().mapToInt(fontRenderer::width).max().orElse(MAX_WIDTH), MAX_WIDTH);
		this.width = TEXT_LEFT + textWidth + 4;
		this.height = Math.max(lines.size() * LINE_HEIGHT + 8, 22);

		this.lines = lines;
		this.display = display;
	}

	@Override
	public Visibility render(PoseStack matrixStack, ToastComponent gui, long time) {
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);

		CLIENT.getTextureManager().bind(TEXTURE);
		this.drawBackground(matrixStack, gui);

		this.drawText(matrixStack);
		this.drawIcon(matrixStack);

		return time >= this.display.visibleTimeMs ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
	}

	private void drawBackground(PoseStack matrixStack, ToastComponent gui) {
		int width = this.width;
		if (width == TEXTURE_WIDTH) {
			this.drawBackgroundRow(matrixStack, gui, 0, 0, width);
			return;
		}

		this.drawBackgroundRow(matrixStack, gui, 0, 0, TEXTURE_BORDER);

		int maxRowWidth = TEXTURE_WIDTH - TEXTURE_BORDER * 2;
		int maxRowX = width - TEXTURE_BORDER;

		int x = TEXTURE_BORDER;
		while (x < maxRowX) {
			int rowWidth = Math.min(maxRowWidth, maxRowX - x);
			this.drawBackgroundRow(matrixStack, gui, x, TEXTURE_BORDER, rowWidth);
			x += rowWidth;
		}

		this.drawBackgroundRow(matrixStack, gui, width - TEXTURE_BORDER, TEXTURE_WIDTH - TEXTURE_BORDER, TEXTURE_BORDER);
	}

	private void drawBackgroundRow(PoseStack matrixStack, ToastComponent gui, int x, int u, int width) {
		int height = this.height;
		int vOffset = this.display.getTextureOffset();

		if (height == TEXTURE_HEIGHT) {
			gui.blit(matrixStack, x, 0, u, vOffset, width, height);
			return;
		}

		gui.blit(matrixStack, x, 0, u, vOffset, width, TEXTURE_BORDER);

		int maxRowHeight = TEXTURE_HEIGHT - TEXTURE_BORDER * 2;
		int maxRowY = height - TEXTURE_BORDER;

		int y = TEXTURE_BORDER;
		while (y < maxRowY) {
			int rowHeight = Math.min(maxRowHeight, maxRowY - y);
			gui.blit(matrixStack, x, y, u, TEXTURE_BORDER + vOffset, width, rowHeight);
			y += rowHeight;
		}

		gui.blit(matrixStack, x, height - TEXTURE_BORDER, u, TEXTURE_HEIGHT - TEXTURE_BORDER + vOffset, width, TEXTURE_BORDER);
	}

	private void drawText(PoseStack matrixStack) {
		Font fontRenderer = CLIENT.font;

		List<FormattedCharSequence> lines = this.lines;
		for (int i = 0; i < lines.size(); i++) {
			FormattedCharSequence line = lines.get(i);
			fontRenderer.draw(matrixStack, line, TEXT_LEFT, 7 + (i * 12), 0xFFFFFFFF);
		}
	}

	private void drawIcon(PoseStack matrixStack) {
		int y = (this.height - ICON_SIZE) / 2;

		NotificationIcon icon = this.display.icon;
		if (icon.item != null) {
			ItemRenderer itemRenderer = CLIENT.getItemRenderer();
			itemRenderer.renderAndDecorateFakeItem(icon.item, 6, y);
		} else if (icon.effect != null) {
			TextureAtlasSprite sprite = CLIENT.getMobEffectTextures().get(icon.effect);
			CLIENT.getTextureManager().bind(sprite.atlas().location());
			GuiComponent.blit(matrixStack, 5, y, 0, 18, 18, sprite);
		}
	}

	@Override
	public int width() {
		return this.width;
	}

	@Override
	public int height() {
		return this.height;
	}
}
