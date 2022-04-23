package com.lovetropics.minigames.client.screen;

import com.lovetropics.minigames.client.screen.flex.Flex;
import com.lovetropics.minigames.client.screen.flex.FlexSolver;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;

public abstract class DynamicLayoutGui extends FocusableGui implements IRenderable {
	
	private final Flex basis;
	protected Layout mainLayout;
	
	public DynamicLayoutGui(Flex basis) {
		this.basis = basis;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.mainLayout.debugRender(matrixStack);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mainLayout.padding().contains(mouseX, mouseY);
	}
	
	public void bake(FlexSolver.Results solve) {
		this.mainLayout = solve.layout(basis);
		for (IGuiEventListener child : children()) {
			if (child instanceof DynamicLayoutGui) {
				((DynamicLayoutGui) child).bake(solve);
			}
		}
	}
}
