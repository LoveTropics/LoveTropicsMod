package com.lovetropics.minigames.client.lobby.manage.screen.game_list;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.screen.TrimmedText;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

// TODO: ideally we extract this out into a general list widget utility that supports all the features we need
public abstract class AbstractGameList extends ExtendedList<AbstractGameList.Entry> {
	private static final int SCROLL_WIDTH = 6;

	private final Screen screen;
	private final ITextComponent title;

	private Entry draggingEntry;
	private int dragOffset;

	public AbstractGameList(Screen screen, Layout layout, ITextComponent title) {
		super(
				Minecraft.getInstance(),
				layout.background().width(), screen.height,
				layout.background().top(), layout.background().bottom(),
				Entry.HEIGHT
		);
		this.setRenderHeader(true, this.minecraft.fontRenderer.FONT_HEIGHT + 4);
		this.setLeftPos(layout.background().left());

		// disable background
		this.func_244605_b(false);
		this.func_244606_c(false);

		this.setRenderSelection(false);

		this.screen = screen;
		this.title = title;
	}

	@Override
	protected void renderHeader(MatrixStack matrixStack, int x, int y, Tessellator tessellator) {
		this.minecraft.fontRenderer.drawText(
				matrixStack, this.title,
				x + (this.width - this.minecraft.fontRenderer.getStringPropertyWidth(this.title)) / 2.0F,
				Math.min(this.y0 + 3, y),
				0xFFFFFF
		);
	}

	@Override
	protected void renderList(MatrixStack matrixStack, int x, int y, int mouseX, int mouseY, float partialTicks) {
		boolean listHovered = this.isMouseOver(mouseX, mouseY);

		int count = this.getItemCount();
		int left = this.getRowLeft();
		int width = this.getRowWidth();
		int height = this.itemHeight;

		boolean dragging = draggingEntry != null;

		for (int index = 0; index < count; index++) {
			int top = this.getRowTop(index);
			int bottom = top + height;
			if (bottom < this.y0 || top > this.y1) continue;

			Entry entry = this.getEntry(index);
			if (draggingEntry == entry) continue;

			boolean entryHovered = !dragging && listHovered && mouseX >= left && mouseY >= top && mouseX < left + width && mouseY < bottom;
			entry.render(matrixStack, index, top, left, width, height, mouseX, mouseY, entryHovered, partialTicks);
		}
	}

	public void renderOverlays(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		renderDragging(matrixStack, mouseX, mouseY, partialTicks);
		renderTooltips(matrixStack, mouseX, mouseY);
	}

	private void renderDragging(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		Entry dragging = this.draggingEntry;
		if (dragging != null) {
			int index = getEventListeners().indexOf(dragging);
			int y = getDraggingY(mouseY);
			dragging.render(matrixStack, index, y, getRowLeft(), getRowWidth(), itemHeight, mouseX, mouseY, false, partialTicks);
		}
	}

	private void renderTooltips(MatrixStack matrixStack, int mouseX, int mouseY) {
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

			Entry entry = this.getEntry(index);
			if (isMouseOverEntry(mouseX, mouseY, entry)) {
				entry.renderTooltips(matrixStack, rowWidth, mouseX, mouseY);
				break;
			}
		}
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

	private boolean isMouseOverEntry(int mouseX, int mouseY, Entry entry) {
		return this.getEntryAtPosition(mouseX, mouseY) == entry;
	}

	private int getEntryIndexAt(int y) {
		int contentY = y - this.y0 - this.headerHeight + (int) this.getScrollAmount();
		return contentY / this.itemHeight;
	}

	void drag(Entry entry, double mouseY) {
		if (this.draggingEntry != entry) {
			this.startDragging(entry, mouseY);
		} else {
			this.moveDragging(entry, mouseY);
		}
	}

	private void moveDragging(Entry entry, double mouseY) {
		List<Entry> entries = this.getEventListeners();
		int index = entries.indexOf(entry);
		int insertIndex = this.getDragInsertIndex(MathHelper.floor(mouseY));
		if (insertIndex != index && insertIndex >= 0 && insertIndex < entries.size()) {
			Entry replaceEntry = entries.get(insertIndex);
			if (replaceEntry.draggable != null) {
				entries.remove(index);
				entries.add(insertIndex, entry);
			}
		}
	}

	private void startDragging(Entry entry, double mouseY) {
		this.draggingEntry = entry;

		int index = this.getEventListeners().indexOf(entry);
		this.dragOffset = MathHelper.floor(this.getRowTop(index) - mouseY);
	}

	private int getDraggingY(int mouseY) {
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
		Entry dragging = this.draggingEntry;
		if (dragging != null) {
			this.stopDragging(dragging);
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	private void stopDragging(Entry dragging) {
		int startIndex = dragging.dragStartIndex;
		int index = this.getEventListeners().indexOf(dragging);
		if (startIndex != index) {
			dragging.draggable.onDragged(index - startIndex);
		}
		this.draggingEntry = null;
	}

	public static final class Entry extends ExtendedList.AbstractListEntry<Entry> {
		public static final int HEIGHT = 32;
		static final int PADDING = 4;

		private final Minecraft client;
		private final AbstractGameList list;

		private final int id;
		private TrimmedText title = TrimmedText.of("");
		private TrimmedText description = null;

		private int backgroundColor = -1;
		private int selectedColor = 0xFF000000;
		private int hoveredColor = 0xFF202020;
		private int outlineColor = 0xFF808080;

		private boolean banner;
		private Draggable draggable;
		private int dragStartIndex;

		public Entry(AbstractGameList list, int id) {
			this.client = list.minecraft;
			this.list = list;

			this.id = id;
		}

		public static Entry game(AbstractGameList list, int id, ClientGameDefinition game) {
			return new Entry(list, id)
					.setTitle(game.name)
					.setDescription(GameTexts.Ui.playerRange(game.minimumParticipants, game.maximumParticipants));
		}

		public Entry setTitle(ITextComponent title) {
			this.title = TrimmedText.of(title);
			return this;
		}

		public Entry setDescription(ITextComponent description) {
			this.description = TrimmedText.of(description);
			return this;
		}

		public Entry setBackgroundColor(int color) {
			this.backgroundColor = color;
			return this;
		}

		public Entry setHoveredColor(int color) {
			this.hoveredColor = color;
			return this;
		}

		public Entry setSelectedColor(int color) {
			this.selectedColor = color;
			return this;
		}

		public Entry setOutlineColor(int color) {
			this.outlineColor = color;
			return this;
		}

		public Entry setBanner(boolean banner) {
			this.banner = banner;
			return this;
		}

		public Entry setDraggable(Draggable draggable) {
			this.draggable = draggable;
			return this;
		}

		@Override
		public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
			FontRenderer font = this.client.fontRenderer;
			int fontHeight = font.FONT_HEIGHT;

			boolean selected = list.isSelectedItem(index);
			boolean outline = banner || selected;

			this.fillEntry(matrixStack, left, top, width, height, hovered, selected, outline);

			int maxTextWidth = getMaxTextWidth(width);

			if (description != null) {
				font.func_238422_b_(matrixStack, title.forWidth(font, maxTextWidth), left + PADDING, top + PADDING + 1, 0xFFFFFF);
				font.func_238422_b_(matrixStack, description.forWidth(font, maxTextWidth), left + PADDING, top + height - PADDING - fontHeight, 0x555555);
			} else {
				font.func_238422_b_(matrixStack, title.forWidth(font, maxTextWidth), left + PADDING, top + (height - fontHeight) / 2, 0xFFFFFF);
			}
		}

		public void renderTooltips(MatrixStack matrixStack, int width, int mouseX, int mouseY) {
			TrimmedText description = this.description;
			int maxTextWidth = getMaxTextWidth(width);
			if (description != null && description.isTrimmedForWidth(client.fontRenderer, maxTextWidth)) {
				list.screen.func_243308_b(matrixStack, ImmutableList.of(description.text()), mouseX, mouseY);
			}
		}

		private static int getMaxTextWidth(int width) {
			return width - 2 * PADDING;
		}

		private void fillEntry(MatrixStack matrixStack, int left, int top, int width, int height, boolean hovered, boolean selected, boolean outline) {
			if (banner) {
				top += 4;
				height -= 8;
			}

			int fillColor = getFillColor(hovered, selected);

			if (outline) {
				fillEntry(matrixStack, left, top, width, height, outlineColor);
				if (fillColor != -1) {
					fillEntry(matrixStack, left + 1, top + 1, width - 2, height - 2, fillColor);
				}
			} else {
				if (fillColor != -1) {
					fillEntry(matrixStack, left, top, width, height, fillColor);
				}
			}
		}

		private int getFillColor(boolean hovered, boolean selected) {
			if (selected) {
				return selectedColor;
			} else if (hovered) {
				return hoveredColor;
			} else {
				return backgroundColor;
			}
		}

		private void fillEntry(MatrixStack matrixStack, int left, int top, int width, int height, int color) {
			fill(matrixStack, left, top, left + width, top + height, color);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			this.list.setSelected(this);
			this.dragStartIndex = this.list.getEventListeners().indexOf(this);
			return true;
		}

		@Override
		public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
			if (this.draggable != null) {
				this.list.drag(this, mouseY);
				return true;
			}
			return false;
		}

		public int getId() {
			return id;
		}
	}

	public interface Draggable {
		void onDragged(int offset);
	}
}
