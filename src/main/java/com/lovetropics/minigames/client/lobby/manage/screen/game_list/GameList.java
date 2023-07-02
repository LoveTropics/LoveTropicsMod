package com.lovetropics.minigames.client.lobby.manage.screen.game_list;

import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyManageState;
import com.lovetropics.minigames.client.screen.flex.Layout;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;

// TODO: name?
public final class GameList implements GuiEventListener, NarratableEntry {
	private final Screen screen;
	private final Layout mainLayout;
	private final Layout footerLayout;

	private final ClientLobbyManageState lobby;

	private final Handlers handlers;

	private AbstractGameList active;

	public GameList(
			Screen screen, Layout main, Layout footer,
			ClientLobbyManageState lobby,
			Handlers handlers
	) {
		this.screen = screen;
		this.mainLayout = main;
		this.footerLayout = footer;
		this.lobby = lobby;
		this.handlers = handlers;

		this.setActive(this.createQueue());
	}

	private GameQueueList createQueue() {
		return new GameQueueList(this.screen, this.mainLayout, this.footerLayout, this.lobby, new GameQueueList.Handlers() {
			@Override
			public void select(int id) {
				GameList.this.handlers.selectQueuedGame(id);
			}

			@Override
			public void enqueue() {
				GameList.this.setActive(GameList.this.createInstalled());
			}

			@Override
			public void remove(int id) {
				GameList.this.handlers.removeQueuedGame(id);
			}

			@Override
			public void reorder(int id, int offset) {
				GameList.this.handlers.reorderQueuedGame(id, offset);
			}
		});
	}

	private InstalledGameList createInstalled() {
		return new InstalledGameList(this.screen, this.mainLayout, this.footerLayout, this.lobby, index -> {
			this.handlers.selectQueuedGame(-1);
			this.handlers.enqueueGame(index);

			this.setActive(this.createQueue());
		});
	}

	private void setActive(AbstractGameList active) {
		active.updateEntries();
		this.active = active;
	}

	public void updateEntries() {
		this.active.updateEntries();
	}

	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.active.render(graphics, mouseX, mouseY, partialTicks);
	}

	public void renderOverlays(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.active.renderOverlays(graphics, mouseX, mouseY, partialTicks);
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

	@Nullable
	@Override
	public ComponentPath nextFocusPath(FocusNavigationEvent event) {
		return this.active.nextFocusPath(event);
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.active.isMouseOver(mouseX, mouseY);
	}

	@Override
	public void setFocused(boolean focused) {
		this.active.setFocused(focused);
	}

	@Override
	public boolean isFocused() {
		return this.active.isFocused();
	}

	@Override
	public NarrationPriority narrationPriority() {
		return this.active.narrationPriority();
	}

	@Override
	public void updateNarration(final NarrationElementOutput output) {
		this.active.updateNarration(output);
	}

	public interface Handlers {
		void selectQueuedGame(int queuedGameId);

		void enqueueGame(int installedGameIndex);

		void removeQueuedGame(int queuedGameId);

		void reorderQueuedGame(int queuedGameId, int offset);
	}
}
