package com.lovetropics.minigames.client.lobby.manage.screen.game_list;

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
import net.minecraft.util.text.ITextComponent;

public abstract class AbstractGameList extends ExtendedList<AbstractGameList.Entry> {
	private static final int SCROLL_WIDTH = 6;

	private final ITextComponent title;

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

	public void renderButtons(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
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
		return this.x1 - SCROLL_WIDTH;
	}

	@Override
	protected int getRowTop(int index) {
		return super.getRowTop(index);
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

		@Override
		public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTicks) {
			FontRenderer font = this.client.fontRenderer;
			int fontHeight = font.FONT_HEIGHT;

			boolean selected = list.isSelectedItem(index);
			boolean outline = banner || selected;

			this.fillEntry(matrixStack, left, top, width, height, hovered, selected, outline);

			int maxTextWidth = list.getRowWidth() - 2 * PADDING;

			if (description != null) {
				font.func_238422_b_(matrixStack, title.forWidth(font, maxTextWidth), left + PADDING, top + PADDING, 0xFFFFFF);
				font.func_238422_b_(matrixStack, description.forWidth(font, maxTextWidth), left + PADDING, top + height - PADDING - fontHeight, 0x555555);
			} else {
				font.func_238422_b_(matrixStack, title.forWidth(font, maxTextWidth), left + PADDING, top + (height - fontHeight) / 2, 0xFFFFFF);
			}
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
			fill(matrixStack, left, top - 2, left + width, top + height + 2, color);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			this.list.setSelected(this);
			return true;
		}

		public int getId() {
			return id;
		}
	}
}
