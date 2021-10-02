package com.lovetropics.minigames.client.screen.list;

import com.lovetropics.minigames.client.screen.list.AbstractLTList.Draggable;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList.AbstractListEntry;

public abstract class LTListEntry<T extends LTListEntry<T>> extends AbstractListEntry<T> {

	protected final Screen screen;
	protected final AbstractLTList<T> list;
	protected Draggable draggable;
	protected int dragStartIndex;

	public LTListEntry(AbstractLTList<T> list, Screen screen) {
		super();
		this.screen = screen;
		this.list = list;
	}

	public void renderTooltips(MatrixStack matrixStack, int width, int mouseX, int mouseY) {}

	@SuppressWarnings("unchecked")
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.list.setSelected((T) this);
		this.dragStartIndex = this.list.getEventListeners().indexOf(this);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (this.draggable != null) {
			this.list.drag((T) this, mouseY);
			return true;
		}
		return false;
	}

}