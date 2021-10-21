package com.lovetropics.minigames.client.screen;

import com.lovetropics.minigames.client.screen.flex.Box;
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
		Box outline = this.mainLayout.background();
		vLine(matrixStack, outline.left(), outline.top(), outline.bottom() - 1, 0xFFFF0000);
		vLine(matrixStack, outline.right() - 1, outline.top(), outline.bottom() - 1, 0xFFFF0000);
		hLine(matrixStack, outline.left(), outline.right() - 1, outline.top(), 0xFFFF0000);
		hLine(matrixStack, outline.left(), outline.right() - 1, outline.bottom() - 1, 0xFFFF0000);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mainLayout.padding().contains(mouseX, mouseY);
	}
	
	public void bake(FlexSolver.Results solve) {
		this.mainLayout = solve.layout(basis);
		for (IGuiEventListener child : getEventListeners()) {
			if (child instanceof DynamicLayoutGui) {
				((DynamicLayoutGui) child).bake(solve);
			}
		}
	}
}
