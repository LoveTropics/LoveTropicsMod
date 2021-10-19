package com.lovetropics.minigames.client.screen;

import com.lovetropics.minigames.client.screen.flex.Layout;

import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IRenderable;

public abstract class LayoutGui extends FocusableGui implements IRenderable {
	
	protected final Layout mainLayout;
	
	public LayoutGui(Layout mainLayout) {
		this.mainLayout = mainLayout;
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mainLayout.padding().contains(mouseX, mouseY);
	}
}
