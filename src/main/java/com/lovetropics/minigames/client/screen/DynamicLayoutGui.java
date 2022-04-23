package com.lovetropics.minigames.client.screen;

import com.lovetropics.minigames.client.screen.flex.Flex;
import com.lovetropics.minigames.client.screen.flex.FlexSolver;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.Widget;

public abstract class DynamicLayoutGui extends AbstractContainerEventHandler implements Widget {
	
	private final Flex basis;
	protected Layout mainLayout;
	
	public DynamicLayoutGui(Flex basis) {
		this.basis = basis;
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.mainLayout.debugRender(matrixStack);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mainLayout.padding().contains(mouseX, mouseY);
	}
	
	public void bake(FlexSolver.Results solve) {
		this.mainLayout = solve.layout(basis);
		for (GuiEventListener child : children()) {
			if (child instanceof DynamicLayoutGui) {
				((DynamicLayoutGui) child).bake(solve);
			}
		}
	}
}
