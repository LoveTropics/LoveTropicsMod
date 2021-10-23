package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.EnumMap;

import com.lovetropics.minigames.client.screen.LayoutTree;
import com.lovetropics.minigames.client.screen.flex.Align;
import com.lovetropics.minigames.client.screen.flex.Axis;
import com.lovetropics.minigames.client.screen.flex.Box;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.util.text.ITextComponent;

public class TextLabel implements IRenderable {
	private final Layout renderArea;
	private final ITextComponent text;
	private final FontRenderer fnt = Minecraft.getInstance().fontRenderer;

	private final EnumMap<Axis, Align.Cross> alignment = new EnumMap<>(Axis.class);
	
	public TextLabel(LayoutTree ltree, int height, ITextComponent text, Align.Cross horizontalAlign, Align.Cross verticalAlign) {
		height = Math.max(height, fnt.FONT_HEIGHT);
		int width = ltree.head().content().width();
		Box margin = new Box();
		int textWidth = fnt.getStringPropertyWidth(text);
		if (horizontalAlign == Align.Cross.CENTER) {
			int spacing = Math.max((width - textWidth) / 2, 0);
			margin = margin.left(spacing).right(spacing);
		} else if (horizontalAlign == Align.Cross.END) {
			margin = margin.shift(width - textWidth, 0);
		}
		if (verticalAlign == Align.Cross.CENTER) {
			int spacing = Math.max((height - fnt.FONT_HEIGHT) / 2, 0);
			margin = margin.top(spacing).bottom(spacing);
		} else if (verticalAlign == Align.Cross.END) {
			margin = margin.shift(0, height - fnt.FONT_HEIGHT);
		}
		ltree.definiteChild(-1, height, new Box(), margin);
		this.renderArea = ltree.pop();
		this.text = text;
		ltree.pop();
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderArea.debugRender(matrixStack);
		fnt.drawText(matrixStack, text, renderArea.content().left(), renderArea.content().top(), -1);
	}
}