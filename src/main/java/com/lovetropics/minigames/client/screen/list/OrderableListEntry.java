package com.lovetropics.minigames.client.screen.list;

import com.lovetropics.minigames.client.screen.list.OrderableSelectionList.Reorder;
import net.minecraft.client.gui.components.ObjectSelectionList.Entry;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nullable;

public abstract class OrderableListEntry<T extends OrderableListEntry<T>> extends Entry<T> {
	protected final Screen screen;
	protected final OrderableSelectionList<T> list;
	@Nullable
	protected Reorder reorder;
	protected int dragStartIndex;

	public OrderableListEntry(OrderableSelectionList<T> list, Screen screen) {
		super();
		this.screen = screen;
		this.list = list;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		list.setSelected((T) this);
		dragStartIndex = list.children().indexOf(this);
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		if (reorder != null) {
			list.drag((T) this, mouseY);
			return true;
		}
		return false;
	}
}
