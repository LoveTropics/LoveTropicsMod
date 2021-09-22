package com.lovetropics.minigames.client.lobby.screen.game_list;

import com.lovetropics.minigames.client.lobby.ClientGameQueueEntry;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntConsumer;

public final class GameQueueList extends AbstractGameList {
	private static final ITextComponent TITLE = new StringTextComponent("Game Queue")
			.mergeStyle(TextFormatting.UNDERLINE, TextFormatting.BOLD);

	private final IntConsumer select;
	private final Runnable enqueue;

	public GameQueueList(Screen screen, Layout layout, IntConsumer select, Runnable enqueue) {
		super(screen, layout, TITLE);

		this.select = select;
		this.enqueue = enqueue;
	}

	public void setEntries(List<ClientGameQueueEntry> games) {
		this.clearEntries();
		for (ClientGameQueueEntry entry : games) {
			this.addEntry(new GameEntry(this, entry.definition));
		}

		this.addEntry(new EnqueueEntry(this));
	}

	@Override
	public void setSelected(@Nullable Entry entry) {
		int index = this.getEventListeners().indexOf(entry);
		this.select.accept(index);

		super.setSelected(entry);
	}

	public static final class EnqueueEntry extends Entry {
		private final Button button;

		EnqueueEntry(GameQueueList list) {
			super(list);

			this.button = new Button(
					PADDING, PADDING,
					20, 20,
					new StringTextComponent("+"), b -> list.enqueue.run()
			);
		}

		@Override
		public void render(MatrixStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
			FontRenderer font = client.fontRenderer;
			font.drawString(matrixStack, "Enqueue", left + 20 + 2 * PADDING, top + (height - font.FONT_HEIGHT) / 2, 0xFFFFFF);

			matrixStack.push();
			matrixStack.translate(left, top, 0.0);
			this.button.render(matrixStack, mouseX - left, mouseY - top, partialTicks);
			matrixStack.pop();
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			double entryMouseX = mouseX - list.getRowLeft();
			double entryMouseY = mouseY - list.getRowTop(list.getEventListeners().indexOf(this));
			return this.button.mouseClicked(entryMouseX, entryMouseY, button);
		}
	}
}
