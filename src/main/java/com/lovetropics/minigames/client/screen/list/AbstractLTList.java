package com.lovetropics.minigames.client.screen.list;

import java.util.List;

import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.util.math.MathHelper;

public abstract class AbstractLTList<T extends LTListEntry<T>> extends ExtendedList<T> {

	private static final int SCROLL_WIDTH = 6;
	protected final Screen screen;
	protected T draggingEntry;
	private int dragOffset;

	public interface Draggable {
		void onDragged(int offset);
	}
	
	public AbstractLTList(Screen screen, Layout layout, int slotHeightIn) {
		super(
				screen.getMinecraft(),
				layout.background().width(), screen.height,
				layout.background().top(), layout.background().bottom(),
				slotHeightIn
		);
		this.screen = screen;
	}

	public void renderOverlays(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderDragging(matrixStack, mouseX, mouseY, partialTicks);
		renderTooltips(matrixStack, mouseX, mouseY);
	}

	protected void renderDragging(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		T dragging = this.draggingEntry;
		if (dragging != null) {
			int index = getEventListeners().indexOf(dragging);
			int y = getDraggingY(mouseY);
			dragging.render(matrixStack, index, y, getRowLeft(), getRowWidth(), itemHeight, mouseX, mouseY, false, partialTicks);
		}
	}

	protected void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY) {
		if (!isMouseOver(mouseX, mouseY) || draggingEntry != null) {
			return;
		}

		int count = this.getItemCount();
		int rowWidth = this.getRowWidth();

		for (int index = 0; index < count; index++) {
			int rowTop = this.getRowTop(index);
			int rowBottom = rowTop + this.itemHeight;
			if (rowBottom < this.y0 || rowTop > this.y1) {
				continue;
			}

			T entry = this.getEntry(index);
			if (isMouseOverEntry(mouseX, mouseY, entry)) {
				entry.renderTooltips(matrixStack, rowWidth, mouseX, mouseY);
				break;
			}
		}
	}

	private boolean isMouseOverEntry(int mouseX, int mouseY, T entry) {
		return this.getEntryAtPosition(mouseX, mouseY) == entry;
	}

	private int getEntryIndexAt(int y) {
		int contentY = y - this.y0 - this.headerHeight + (int) this.getScrollAmount();
		return contentY / this.itemHeight;
	}

	public abstract void updateEntries();

	@Override
	public int getRowLeft() {
		return this.x0;
	}

	@Override
	public int getRowWidth() {
		return this.getMaxScroll() > 0 ? this.width - SCROLL_WIDTH : this.width;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.getMaxScroll() > 0 ? this.x1 - SCROLL_WIDTH : this.x1;
	}

	@Override
	protected int getRowTop(int index) {
		return this.y0 + this.headerHeight - (int) this.getScrollAmount()
				+ index * this.itemHeight;
	}

	void drag(T entry, double mouseY) {
		if (this.draggingEntry != entry) {
			this.startDragging(entry, mouseY);
		} else {
			this.moveDragging(entry, mouseY);
		}
	}

	private void moveDragging(T entry, double mouseY) {
		List<T> entries = this.getEventListeners();
		int index = entries.indexOf(entry);
		int insertIndex = this.getDragInsertIndex(MathHelper.floor(mouseY));
		if (insertIndex != index && insertIndex >= 0 && insertIndex < entries.size()) {
			T replaceEntry = entries.get(insertIndex);
			if (replaceEntry.draggable != null) {
				entries.remove(index);
				entries.add(insertIndex, entry);
			}
		}
	}

	private void startDragging(T entry, double mouseY) {
		this.draggingEntry = entry;
	
		int index = this.getEventListeners().indexOf(entry);
		this.dragOffset = MathHelper.floor(this.getRowTop(index) - mouseY);
	}

	protected int getDraggingY(int mouseY) {
		int draggingY = mouseY + dragOffset;
		int minY = this.y0 + this.headerHeight;
		int maxY = this.y1 - this.itemHeight;
		return MathHelper.clamp(draggingY, minY, maxY);
	}

	private int getDragInsertIndex(int mouseY) {
		return getEntryIndexAt(getDraggingY(mouseY) + itemHeight / 2);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		T dragging = this.draggingEntry;
		if (dragging != null) {
			this.stopDragging(dragging);
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	private void stopDragging(T dragging) {
		int startIndex = dragging.dragStartIndex;
		int index = this.getEventListeners().indexOf(dragging);
		if (startIndex != index) {
			dragging.draggable.onDragged(index - startIndex);
		}
		this.draggingEntry = null;
	}
}