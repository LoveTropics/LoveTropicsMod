package com.lovetropics.minigames.client.lobby.screen;

import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public final class GameQueueList extends ExtendedList<GameQueueList.Entry> {
	private static final ITextComponent TITLE = new StringTextComponent("Game Queue")
			.mergeStyle(TextFormatting.UNDERLINE, TextFormatting.BOLD);

	public GameQueueList(Screen screen, Layout layout) {
		super(
				Minecraft.getInstance(),
				layout.background().width(), screen.height,
				layout.background().top(), layout.background().bottom(),
				Entry.HEIGHT
		);
		this.setRenderHeader(true, this.minecraft.fontRenderer.FONT_HEIGHT * 3 / 2);
		this.setLeftPos(layout.background().left());
	}

	public void addEntries() {
		this.clearEntries();
		this.addEntry(new Entry(this.minecraft, new StringTextComponent("Test Game")));
	}

	@Override
	protected void renderHeader(MatrixStack matrixStack, int x, int y, Tessellator tessellator) {
		this.minecraft.fontRenderer.drawText(
				matrixStack, TITLE,
				x + (this.width - this.minecraft.fontRenderer.getStringPropertyWidth(TITLE)) / 2.0F,
				Math.min(this.y0 + 3, y),
				0xFFFFFF
		);
	}

	@Override
	public int getRowWidth() {
		return this.width;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.x1 - 6;
	}

	public static final class Entry extends ExtendedList.AbstractListEntry<Entry> {
		public static final int HEIGHT = 32;

		private final Minecraft client;
		private final ITextComponent name;

		public Entry(Minecraft client, ITextComponent name) {
			this.client = client;
			this.name = name;
		}

		@Override
		public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
			this.client.fontRenderer.drawText(matrixStack, this.name, left + 32 + 3, top + 1, 0xFFFFFF);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		public boolean mouseReleased(double mouseX, double mouseY, int button) {
			return super.mouseReleased(mouseX, mouseY, button);
		}
	}
}
