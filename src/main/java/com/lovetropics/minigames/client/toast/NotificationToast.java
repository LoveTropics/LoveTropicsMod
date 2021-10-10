package com.lovetropics.minigames.client.toast;

import com.lovetropics.minigames.Constants;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.ToastGui;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public final class NotificationToast implements IToast {
	private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MODID, "textures/gui/toasts.png");

	private static final int TEXTURE_WIDTH = 160;
	private static final int TEXTURE_HEIGHT = 32;
	private static final int TEXTURE_BORDER = 4;

	private static final int ICON_SIZE = 18;
	private static final int TEXT_LEFT = ICON_SIZE + 8;
	private static final int MAX_WIDTH = 160 - TEXT_LEFT;

	private static final int LINE_HEIGHT = 12;

	private static final Minecraft CLIENT = Minecraft.getInstance();

	private final List<IReorderingProcessor> lines;
	private final NotificationDisplay display;

	private final int width;
	private final int height;

	public NotificationToast(ITextComponent message, NotificationDisplay display) {
		FontRenderer fontRenderer = CLIENT.fontRenderer;

		List<IReorderingProcessor> lines = new ArrayList<>(2);
		lines.addAll(fontRenderer.trimStringToWidth(message, MAX_WIDTH));

		int textWidth = Math.max(lines.stream().mapToInt(fontRenderer::func_243245_a).max().orElse(MAX_WIDTH), MAX_WIDTH);
		this.width = TEXT_LEFT + textWidth + 4;
		this.height = Math.max(lines.size() * LINE_HEIGHT + 8, 22);

		this.lines = lines;
		this.display = display;
	}

	@Override
	public Visibility func_230444_a_(MatrixStack matrixStack, ToastGui gui, long time) {
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);

		CLIENT.getTextureManager().bindTexture(TEXTURE);
		this.drawBackground(matrixStack, gui);

		this.drawText(matrixStack);
		this.drawIcon(matrixStack);

		return time >= this.display.visibleTimeMs ? IToast.Visibility.HIDE : IToast.Visibility.SHOW;
	}

	private void drawBackground(MatrixStack matrixStack, ToastGui gui) {
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

	private void drawBackgroundRow(MatrixStack matrixStack, ToastGui gui, int x, int u, int width) {
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

	private void drawText(MatrixStack matrixStack) {
		FontRenderer fontRenderer = CLIENT.fontRenderer;

		List<IReorderingProcessor> lines = this.lines;
		for (int i = 0; i < lines.size(); i++) {
			IReorderingProcessor line = lines.get(i);
			fontRenderer.func_238422_b_(matrixStack, line, TEXT_LEFT, 7 + (i * 12), 0xFFFFFFFF);
		}
	}

	private void drawIcon(MatrixStack matrixStack) {
		int y = (this.height - ICON_SIZE) / 2;

		NotificationIcon icon = this.display.icon;
		if (icon.item != null) {
			ItemRenderer itemRenderer = CLIENT.getItemRenderer();
			itemRenderer.renderItemAndEffectIntoGuiWithoutEntity(icon.item, 6, y);
		} else if (icon.effect != null) {
			TextureAtlasSprite sprite = CLIENT.getPotionSpriteUploader().getSprite(icon.effect);
			CLIENT.getTextureManager().bindTexture(sprite.getAtlasTexture().getTextureLocation());
			AbstractGui.blit(matrixStack, 5, y, 0, 18, 18, sprite);
		}
	}

	@Override
	public int func_230445_a_() {
		return this.width;
	}

	@Override
	public int func_238540_d_() {
		return this.height;
	}
}
