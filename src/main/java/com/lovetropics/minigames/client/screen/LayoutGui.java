package com.lovetropics.minigames.client.screen;

import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IRenderable;

public abstract class LayoutGui extends FocusableGui implements IRenderable {
	
	protected Layout mainLayout;
	
	public LayoutGui() {
		this(null);
	}
	
	public LayoutGui(Layout mainLayout) {
		this.mainLayout = mainLayout;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mainLayout.padding().contains(mouseX, mouseY);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.mainLayout.debugRender(matrixStack);
	}
}
