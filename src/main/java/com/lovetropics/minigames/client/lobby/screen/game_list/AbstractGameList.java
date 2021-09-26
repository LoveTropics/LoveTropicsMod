package com.lovetropics.minigames.client.lobby.screen.game_list;

import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.LanguageMap;

public abstract class AbstractGameList extends ExtendedList<AbstractGameList.Entry> {
	private static final int SCROLL_WIDTH = 6;

	private final ITextComponent title;

	public AbstractGameList(Screen screen, Layout layout, ITextComponent title) {
		super(
				Minecraft.getInstance(),
				layout.background().width() - SCROLL_WIDTH, screen.height,
				layout.background().top(), layout.background().bottom(),
				Entry.HEIGHT
		);
		this.setRenderHeader(true, this.minecraft.fontRenderer.FONT_HEIGHT + 4);
		this.setLeftPos(layout.background().left());

		// disable background
		this.func_244605_b(false);
		this.func_244606_c(false);

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

	@Override
	public int getRowWidth() {
		return this.width;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.x1;
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

		private final IReorderingProcessor name;
		private final String description;

		public Entry(AbstractGameList list, ClientGameDefinition game) {
			this.client = list.minecraft;
			this.list = list;

			FontRenderer font = this.client.fontRenderer;
			int maxTextWidth = list.getRowWidth() - 2 * PADDING;

			this.name = LanguageMap.getInstance().func_241870_a(font.func_238417_a_(game.name, maxTextWidth));
			this.description = font.trimStringToWidth(description(game), maxTextWidth);
		}

		private static String description(ClientGameDefinition game) {
			if (game.maximumParticipants != game.minimumParticipants) {
				return game.minimumParticipants + "-" + game.maximumParticipants + " players";
			} else {
				return game.minimumParticipants + " players";
			}
		}

		@Override
		public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
			FontRenderer font = this.client.fontRenderer;
			int fontHeight = font.FONT_HEIGHT;

			if (isMouseOver) {
				fill(matrixStack, left, top, left + width - 4, top + height, 0xFF000000);
			}

			font.func_238422_b_(matrixStack, this.name, left + PADDING, top + PADDING, 0xFFFFFF);
			font.drawString(matrixStack, this.description, left + PADDING, top + height - PADDING - fontHeight, 0x555555);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			this.list.setSelected(this);
			return true;
		}
	}
}
