package com.lovetropics.minigames.client.screen;

import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.Widget;

public abstract class LayoutGui extends AbstractContainerEventHandler implements Widget {
	
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
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.mainLayout.debugRender(matrixStack);
	}
}
