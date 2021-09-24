package com.lovetropics.minigames.client.lobby.screen.game_list;

import com.lovetropics.minigames.client.lobby.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.ClientQueuedGame;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;

import java.util.List;

// TODO: name?
public final class GameList implements IGuiEventListener {
	private final Screen screen;
	private final Layout mainLayout;
	private final Layout footerLayout;

	private final List<ClientQueuedGame> queue;
	private final List<ClientGameDefinition> installedGames;

	private final Handlers handlers;

	private AbstractGameList active;

	public GameList(
			Screen screen, Layout main, Layout footer,
			List<ClientQueuedGame> queue, List<ClientGameDefinition> installedGames,
			Handlers handlers
	) {
		this.screen = screen;
		this.mainLayout = main;
		this.footerLayout = footer;
		this.queue = queue;
		this.installedGames = installedGames;
		this.handlers = handlers;

		this.active = this.createQueue();
	}

	private GameQueueList createQueue() {
		GameQueueList queue = new GameQueueList(this.screen, this.mainLayout, this.footerLayout, new GameQueueList.Handlers() {
			@Override
			public void select(int index) {
				GameList.this.handlers.selectQueuedGame(index);
			}

			@Override
			public void enqueue() {
				GameList.this.active = GameList.this.createInstalled();
			}

			@Override
			public void remove(int index) {
				GameList.this.handlers.removeQueuedGame(index);
			}
		});
		queue.setEntries(this.queue);

		return queue;
	}

	private InstalledGameList createInstalled() {
		InstalledGameList installed = new InstalledGameList(this.screen, this.mainLayout, this.footerLayout, index -> {
			this.handlers.selectQueuedGame(-1);
			this.handlers.enqueueGame(index);

			this.active = this.createQueue();
		});
		installed.setEntries(this.installedGames);

		return installed;
	}

	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.active.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	public void renderButtons(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.active.renderButtons(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		this.active.mouseMoved(mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return this.active.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return this.active.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
		return this.active.mouseDragged(mouseX, mouseY, button, dragX, dragY);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		return this.active.mouseScrolled(mouseX, mouseY, delta);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return this.active.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return this.active.keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char codePoint, int modifiers) {
		return this.active.charTyped(codePoint, modifiers);
	}

	@Override
	public boolean changeFocus(boolean focus) {
		return this.active.changeFocus(focus);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.active.isMouseOver(mouseX, mouseY);
	}

	public interface Handlers {
		void selectQueuedGame(int queuedGameIndex);

		void enqueueGame(int installedGameIndex);

		void removeQueuedGame(int queuedGameIndex);
	}
}
