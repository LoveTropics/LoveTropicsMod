package com.lovetropics.minigames.client.lobby.manage.screen;

import com.lovetropics.minigames.client.lobby.manage.ClientLobbyManagement;
import com.lovetropics.minigames.client.lobby.manage.screen.game_list.GameList;
import com.lovetropics.minigames.client.lobby.manage.screen.player_list.LobbyPlayerList;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyManageState;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueue;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueuedGame;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.screen.FlexUi;
import com.lovetropics.minigames.client.screen.flex.Box;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.List;

// TODO: localisation
public final class ManageLobbyScreen extends Screen {
	private static final ITextComponent TITLE = new StringTextComponent("Manage Game Lobby");

	private final ClientLobbyManagement.Session session;

	private ManageLobbyLayout layout;

	private TextFieldWidget nameField;
	private GameList gameList;
	private LobbyPlayerList playerList;

	private Button playButton;
	private Button skipButton;

	private int selectedGameId = -1;

	public ManageLobbyScreen(ClientLobbyManagement.Session session) {
		super(TITLE);
		this.session = session;
	}

	@Override
	protected void init() {
		minecraft.keyboardListener.enableRepeatEvents(true);

		selectedGameId = -1;

		layout = new ManageLobbyLayout(this);

		ClientLobbyManageState lobby = session.lobby();

		gameList = addListener(new GameList(this, layout.gameList, layout.leftFooter, lobby, new GameList.Handlers() {
			@Override
			public void selectQueuedGame(int queuedGameId) {
				selectedGameId = queuedGameId;
			}

			@Override
			public void enqueueGame(int installedGameIndex) {
				List<ClientGameDefinition> installedGames = lobby.getInstalledGames();
				if (installedGameIndex >= 0 && installedGameIndex < installedGames.size()) {
					session.enqueueGame(installedGames.get(installedGameIndex));
				}
			}

			@Override
			public void removeQueuedGame(int queuedGameId) {
				session.removeQueuedGame(queuedGameId);
			}
		}));

		nameField = addListener(FlexUi.createTextField(layout.name, font, new StringTextComponent("Lobby Name")));
		nameField.setMaxStringLength(200);
		nameField.setText(lobby.getName());

		playerList = addListener(new LobbyPlayerList(lobby, layout.playerList));

		addButton(FlexUi.createButton(layout.done, DialogTexts.GUI_DONE, b -> closeScreen()));

		playButton = addButton(FlexUi.createButton(layout.play, new StringTextComponent("\u25B6"), b -> {
			session.selectControl(LobbyControls.Type.PLAY);
		}));
		skipButton = addButton(FlexUi.createButton(layout.skip, new StringTextComponent("\u23ED"), b -> {
			session.selectControl(LobbyControls.Type.SKIP);
		}));

		setControlsState();
	}

	// TODO: custom text field instance
	@Override
	public void setListener(@Nullable IGuiEventListener listener) {
		IGuiEventListener lastListener = this.getListener();
		if (lastListener != null && lastListener != listener) {
			this.onLoseFocus(lastListener);
		}
		super.setListener(listener);
	}

	private void onLoseFocus(IGuiEventListener listener) {
		if (listener == nameField) {
			applyNameField();
		}
	}

	public void updateGameList() {
		gameList.updateEntries();
	}

	public void updateNameField() {
		ClientLobbyManageState lobby = session.lobby();
		nameField.setText(lobby.getName());
	}

	public void setControlsState() {
		ClientLobbyManageState lobby = session.lobby();
		LobbyControls.State controls = lobby.getControlsState();
		playButton.active = controls.enabled(LobbyControls.Type.PLAY);
		skipButton.active = controls.enabled(LobbyControls.Type.SKIP);
	}

	@Override
	public void closeScreen() {
		super.closeScreen();

		session.close();

		applyNameField();
	}

	private void applyNameField() {
		String name = nameField.getText();
		if (!name.equals(session.lobby().getName())) {
			session.setName(name);
		}
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int fontHeight = font.FONT_HEIGHT;

		renderBackground(matrixStack, 0);

		FlexUi.fill(layout.leftColumn, matrixStack, 0x80101010);
		FlexUi.fill(layout.rightColumn, matrixStack, 0x80101010);

		gameList.render(matrixStack, mouseX, mouseY, partialTicks);

		for (Layout marginal : layout.marginals) {
			FlexUi.fill(marginal, matrixStack, 0xFF101010);
		}

		gameList.renderButtons(matrixStack, mouseX, mouseY, partialTicks);

		// TODO: make this name rendering better
		drawString(matrixStack, font, nameField.getMessage(), nameField.x, nameField.y - fontHeight - 2, 0xFFFFFF);
		nameField.render(matrixStack, mouseX, mouseY, partialTicks);

		playerList.render(matrixStack, mouseX, mouseY, partialTicks);

		Box header = layout.header.content();
		drawCenteredString(matrixStack, font, title, header.centerX(), header.centerY(), 0xFFFFFF);

		ClientLobbyQueue queue = session.lobby().getQueue();
		ClientLobbyQueuedGame selectedEntry = queue.byId(selectedGameId);
		if (selectedEntry != null) {
			renderSelectedGame(selectedEntry, matrixStack, mouseX, mouseY, partialTicks);
		}

		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	private void renderSelectedGame(ClientLobbyQueuedGame game, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		FlexUi.fill(layout.centerHeader, matrixStack, 0x80101010);

		ITextComponent title = new StringTextComponent("")
				.appendSibling(new StringTextComponent("Managing: ").mergeStyle(TextFormatting.BOLD))
				.appendSibling(game.definition().name);

		Box header = layout.centerHeader.content();
		drawCenteredString(matrixStack, font, title, header.centerX(), header.centerY() - font.FONT_HEIGHT / 2, 0xFFFFFF);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
