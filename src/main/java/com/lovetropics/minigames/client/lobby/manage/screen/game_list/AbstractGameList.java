package com.lovetropics.minigames.client.lobby.manage.screen.game_list;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.screen.TrimmedText;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.client.screen.list.AbstractLTList;
import com.lovetropics.minigames.client.screen.list.LTListEntry;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.ITextComponent;

// TODO: ideally we extract this out into a general list widget utility that supports all the features we need
public abstract class AbstractGameList extends AbstractLTList<AbstractGameList.Entry> {
	private final ITextComponent title;

	public AbstractGameList(Screen screen, Layout layout, ITextComponent title) {
		super(screen, layout, Entry.HEIGHT);
		this.setRenderHeader(true, this.minecraft.fontRenderer.FONT_HEIGHT + 4);
		this.setLeftPos(layout.background().left());

		// disable background
		this.func_244605_b(false);
		this.func_244606_c(false);

		this.setRenderSelection(false);

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

			LTListEntry entry = this.getEntry(index);
			if (draggingEntry == entry) continue;

			boolean entryHovered = !dragging && listHovered && mouseX >= left && mouseY >= top && mouseX < left + width && mouseY < bottom;
			entry.render(matrixStack, index, top, left, width, height, mouseX, mouseY, entryHovered, partialTicks);
		}
	}

	public static final class Entry extends LTListEntry<Entry> {
		public static final int HEIGHT = 32;
		static final int PADDING = 4;

		private final int id;
		TrimmedText title = TrimmedText.of("");
		TrimmedText description = null;

		private int backgroundColor = -1;
		private int selectedColor = 0xFF000000;
		private int hoveredColor = 0xFF202020;
		private int outlineColor = 0xFF808080;

		boolean banner;
		public Entry(AbstractGameList list, int id) {
			super(list, list.screen);
			this.id = id;
		}

		public static Entry game(AbstractGameList list, int id, ClientGameDefinition game) {
			return new Entry(list, id)
					.setTitle(game.name)
					.setDescription(GameTexts.Ui.playerRange(game.minimumParticipants, game.maximumParticipants));
		}
		
		@Override
		public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
			FontRenderer font = screen.getMinecraft().fontRenderer;
			int fontHeight = font.FONT_HEIGHT;
		
			boolean selected = ((AbstractGameList)list).isSelectedItem(index);
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

		@Override
		public void renderTooltips(MatrixStack matrixStack, int width, int mouseX, int mouseY) {
			super.renderTooltips(matrixStack, width, mouseX, mouseY);
			TrimmedText description = this.description;
			int maxTextWidth = getMaxTextWidth(width);
			if (description != null && description.isTrimmedForWidth(screen.getMinecraft().fontRenderer, maxTextWidth)) {
				screen.func_243308_b(matrixStack, ImmutableList.of(description.text()), mouseX, mouseY);
			}
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

		static int getMaxTextWidth(int width) {
			return width - 2 * PADDING;
		}

		void fillEntry(MatrixStack matrixStack, int left, int top, int width, int height, boolean hovered, boolean selected, boolean outline) {
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
			AbstractGameList.fill(matrixStack, left, top, left + width, top + height, color);
		}

		public int getId() {
			return id;
		}
	}
}
