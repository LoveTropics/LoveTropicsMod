package com.lovetropics.minigames.client.screen.list;

import com.lovetropics.minigames.client.screen.list.AbstractLTList.Reorder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.ObjectSelectionList.Entry;

public abstract class LTListEntry<T extends LTListEntry<T>> extends Entry<T> {

	protected final Screen screen;
	protected final AbstractLTList<T> list;
	protected Reorder reorder;
	protected int dragStartIndex;

	public LTListEntry(AbstractLTList<T> list, Screen screen) {
		super();
		this.screen = screen;
		this.list = list;
	}

	public void renderTooltips(PoseStack matrixStack, int width, int mouseX, int mouseY) {}

	@SuppressWarnings("unchecked")
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		this.list.setSelected((T) this);
		this.dragStartIndex = this.list.children().indexOf(this);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (this.reorder != null) {
			this.list.drag((T) this, mouseY);
			return true;
		}
		return false;
	}

}
