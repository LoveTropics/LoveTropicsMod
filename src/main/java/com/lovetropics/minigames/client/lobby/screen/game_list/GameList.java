package com.lovetropics.minigames.client.lobby.screen.game_list;

import com.lovetropics.minigames.client.lobby.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.ClientGameQueueEntry;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;

// TODO: name?
public final class GameList implements IGuiEventListener {
	private final Handlers handlers;

	private final GameQueueList queue;
	private final InstalledGameList installed;

	private boolean enqueuing;

	public GameList(Screen screen, Layout layout, Handlers handlers) {
		this.handlers = handlers;

		this.queue = new GameQueueList(screen, layout, this::selectQueuedGame, this::enqueueGame);
		this.installed = new InstalledGameList(screen, layout, this::selectInstalled);
	}

	public void setEntries(List<ClientGameQueueEntry> queue, List<ClientGameDefinition> installedGames) {
		this.queue.setEntries(queue);
		this.installed.setEntries(installedGames);
	}

	private void selectQueuedGame(int index) {
		this.handlers.selectQueuedGame(index);
	}

	private void enqueueGame() {
		this.enqueuing = true;
	}

	private void selectInstalled(int index) {
		this.handlers.selectQueuedGame(-1);
		this.handlers.enqueueGame(index);
		this.enqueuing = false;
	}

	private AbstractGameList active() {
		return this.enqueuing ? this.installed : this.queue;
	}

	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.active().render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		this.active().mouseMoved(mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return this.active().mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return this.active().mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return this.active().mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		return this.active().mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return this.active().keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return this.active().keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		return this.active().charTyped(codePoint, modifiers);
	}

	@Override
	public boolean changeFocus(boolean focus) {
		return this.active().changeFocus(focus);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.active().isMouseOver(mouseX, mouseY);
	}

	public interface Handlers {
		void selectQueuedGame(int queuedGameIndex);

		void enqueueGame(int installedGameIndex);
	}
}
