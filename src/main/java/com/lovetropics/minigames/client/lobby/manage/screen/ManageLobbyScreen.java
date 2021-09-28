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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

// TODO: localisation
public final class ManageLobbyScreen extends Screen {
	private static final ITextComponent TITLE = new StringTextComponent("Manage Game Lobby");

	private final ClientLobbyManagement.Session session;

	private ManageLobbyLayout layout;

	private TextFieldWidget nameField;
	private GameList gameList;
	private LobbyPlayerList playerList;

	private Button pauseButton;
	private Button playButton;
	private Button stopButton;

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

		initGamesList();
		initNameField();

		ClientLobbyManageState lobby = session.lobby();

		playerList = addListener(new LobbyPlayerList(lobby, layout.playerList));

		addButton(FlexUi.createButton(layout.done, DialogTexts.GUI_DONE, b -> closeScreen()));

		pauseButton = addButton(FlexUi.createButton(layout.pause, new StringTextComponent("\u23F8"), b -> {}));
		playButton = addButton(FlexUi.createButton(layout.play, new StringTextComponent("\u25B6"), b -> {}));
		stopButton = addButton(FlexUi.createButton(layout.stop, new StringTextComponent("\u23F9"), b -> {}));

		setControlsState();
	}

	public void initGamesList() {
		ClientLobbyManageState lobby = session.lobby();

		// TODO: be able to update without recreating
		if (gameList != null) {
			children.remove(gameList);
		}

		gameList = addListener(new GameList(this, layout.gameList, layout.leftFooter, lobby.getQueue(), lobby.getInstalledGames(), new GameList.Handlers() {
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
	}

	public void initNameField() {
		ClientLobbyManageState lobby = session.lobby();

		// TODO: be able to update without recreating
		if (nameField != null) {
			children.remove(nameField);
		}

		nameField = addListener(FlexUi.createTextField(layout.name, font, new StringTextComponent("Lobby Name")));
		nameField.setMaxStringLength(200);
		nameField.setText(lobby.getName());
		setFocusedDefault(nameField);
	}

	public void setControlsState() {
		ClientLobbyManageState lobby = session.lobby();
		LobbyControls.State controls = lobby.getControlsState();
		pauseButton.active = controls.enabled(LobbyControls.Type.PAUSE);
		playButton.active = controls.enabled(LobbyControls.Type.PLAY);
		stopButton.active = controls.enabled(LobbyControls.Type.STOP);
	}

	@Override
	public void closeScreen() {
		super.closeScreen();

		// TODO: move out of here?
		String name = nameField.getText();
		if (!name.equals(session.lobby().getName())) {
			session.setName(name);
		}

		session.close();
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

	private void renderSelectedGame(ClientLobbyQueuedGame entry, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		FlexUi.fill(layout.centerHeader, matrixStack, 0x80101010);

		ITextComponent title = new StringTextComponent("")
				.appendSibling(new StringTextComponent("Managing: ").mergeStyle(TextFormatting.BOLD))
				.appendSibling(entry.definition().name);

		Box header = layout.centerHeader.content();
		drawCenteredString(matrixStack, font, title, header.centerX(), header.centerY() - font.FONT_HEIGHT / 2, 0xFFFFFF);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
