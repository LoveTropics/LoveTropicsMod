package com.lovetropics.minigames.client.lobby.manage.screen.game_list;

import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueue;
import com.lovetropics.minigames.client.screen.FlexUi;
import com.lovetropics.minigames.client.screen.flex.Flex;
import com.lovetropics.minigames.client.screen.flex.FlexSolver;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;

public final class GameQueueList extends AbstractGameList {
	private static final ITextComponent TITLE = new StringTextComponent("Game Queue")
			.mergeStyle(TextFormatting.UNDERLINE, TextFormatting.BOLD);

	private final Handlers handlers;

	private final Button enqueueButton;
	private final Button removeButton;

	public GameQueueList(Screen screen, Layout main, Layout footer, Handlers handlers) {
		super(screen, main, TITLE);
		this.handlers = handlers;

		Flex root = new Flex().rows();
		Flex enqueue = root.child().size(20, 20).marginRight(2);
		Flex cancel = root.child().size(20, 20).marginLeft(2);

		FlexSolver.Results solve = new FlexSolver(footer.content()).apply(root);
		this.enqueueButton = FlexUi.createButton(solve.layout(enqueue), new StringTextComponent("+"), this::enqueue);
		this.removeButton = FlexUi.createButton(solve.layout(cancel), new StringTextComponent("-"), this::remove);

		this.setSelected(null);
	}

	public void setEntries(ClientLobbyQueue queue) {
		this.setSelected(null);

		this.clearEntries();

		for (ClientLobbyQueue.Entry entry : queue.entries()) {
			this.addEntry(new Entry(this, entry.id(), entry.game().definition()));
		}
	}

	private void enqueue(Button button) {
		this.handlers.enqueue();
	}

	private void remove(Button button) {
		Entry selected = this.getSelected();
		this.setSelected(null);

		if (selected != null && this.removeEntry(selected)) {
			this.handlers.remove(selected.getId());
		}
	}

	@Override
	public void setSelected(@Nullable Entry entry) {
		this.handlers.select(entry != null ? entry.getId() : -1);
		this.removeButton.active = entry != null;

		super.setSelected(entry);
	}

	@Override
	public void renderButtons(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.enqueueButton.render(matrixStack, mouseX, mouseY, partialTicks);
		this.removeButton.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.enqueueButton.mouseClicked(mouseX, mouseY, button) || this.removeButton.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	public interface Handlers {
		void select(int id);

		void enqueue();

		void remove(int id);
	}
}