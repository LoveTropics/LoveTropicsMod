package com.lovetropics.minigames.client.lobby.screen;

import com.lovetropics.minigames.client.lobby.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.ClientQueuedGame;
import com.lovetropics.minigames.client.lobby.screen.game_list.GameList;
import com.lovetropics.minigames.client.lobby.screen.player_list.LobbyPlayerList;
import com.lovetropics.minigames.client.screen.FlexUi;
import com.lovetropics.minigames.client.screen.flex.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

// TODO: localisation
public final class ManageLobbyScreen extends Screen {
	private static final ITextComponent TITLE = new StringTextComponent("Manage Game Lobby");

	private final String name;
	private final List<ClientQueuedGame> queue;
	private final List<ClientGameDefinition> installedGames;

	private Layouts layouts;

	private TextFieldWidget nameField;
	private GameList gameList;
	private LobbyPlayerList playerList;

	private int selectedGameIndex = -1;

	public ManageLobbyScreen(String name, List<ClientQueuedGame> queue, List<ClientGameDefinition> installedGames) {
		super(TITLE);
		this.name = name;
		this.queue = queue;
		this.installedGames = installedGames;
	}

	@Override
	protected void init() {
		selectedGameIndex = -1;

		layouts = new Layouts(this);

		minecraft.keyboardListener.enableRepeatEvents(true);

		gameList = addListener(new GameList(this, layouts.gameList, layouts.leftFooter, queue, installedGames, new GameList.Handlers() {
			@Override
			public void selectQueuedGame(int queuedGameIndex) {
				selectedGameIndex = queuedGameIndex;
			}

			@Override
			public void enqueueGame(int installedGameIndex) {
				if (installedGameIndex >= 0 && installedGameIndex < installedGames.size()) {
					queue.add(new ClientQueuedGame(installedGames.get(installedGameIndex)));
				}
			}

			@Override
			public void removeQueuedGame(int queuedGameIndex) {
				if (queuedGameIndex >= 0 && queuedGameIndex < queue.size()) {
					queue.remove(queuedGameIndex);
				}
			}
		}));

		nameField = addListener(FlexUi.createTextField(layouts.name, font, new StringTextComponent("Lobby Name")));
		nameField.setMaxStringLength(128);
		nameField.setText(name);
		setFocusedDefault(nameField);

		playerList = addListener(new LobbyPlayerList(layouts.playerList));

		addButton(FlexUi.createButton(layouts.done, DialogTexts.GUI_DONE, b -> closeScreen()));

		addButton(FlexUi.createButton(layouts.pause, new StringTextComponent("\u23F8"), b -> {}));
		addButton(FlexUi.createButton(layouts.play, new StringTextComponent("\u25B6"), b -> {}));
		addButton(FlexUi.createButton(layouts.stop, new StringTextComponent("\u23F9"), b -> {}));
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int fontHeight = font.FONT_HEIGHT;

		renderBackground(matrixStack, 0);

		FlexUi.fill(layouts.leftColumn, matrixStack, 0x80101010);
		FlexUi.fill(layouts.rightColumn, matrixStack, 0x80101010);

		gameList.render(matrixStack, mouseX, mouseY, partialTicks);

		for (Layout marginal : layouts.marginals) {
			FlexUi.fill(marginal, matrixStack, 0xFF101010);
		}

		gameList.renderButtons(matrixStack, mouseX, mouseY, partialTicks);

		// TODO: make this name rendering better
		drawString(matrixStack, font, nameField.getMessage(), nameField.x, nameField.y - fontHeight - 2, 0xFFFFFF);
		nameField.render(matrixStack, mouseX, mouseY, partialTicks);

		playerList.render(matrixStack, mouseX, mouseY, partialTicks);

		Box header = layouts.header.content();
		drawCenteredString(matrixStack, font, title, header.centerX(), header.centerY(), 0xFFFFFF);

		if (selectedGameIndex >= 0 && selectedGameIndex < queue.size()) {
			ClientQueuedGame selectedEntry = queue.get(selectedGameIndex);
			renderSelectedGame(selectedEntry, matrixStack, mouseX, mouseY, partialTicks);
		}

		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	private void renderSelectedGame(ClientQueuedGame entry, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		FlexUi.fill(layouts.centerHeader, matrixStack, 0x80101010);

		ITextComponent title = new StringTextComponent("")
				.appendSibling(new StringTextComponent("Managing: ").mergeStyle(TextFormatting.BOLD))
				.appendSibling(entry.definition.name);

		Box header = layouts.centerHeader.content();
		drawCenteredString(matrixStack, font, title, header.centerX(), header.centerY() - font.FONT_HEIGHT / 2, 0xFFFFFF);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public static final class Layouts {
		static final int PADDING = 8;
		static final int FOOTER_HEIGHT = 20;

		final Layout header;

		final Layout leftColumn;
		final Layout leftFooter;

		final Layout gameList;

		final Layout centerColumn;
		final Layout centerFooter;

		final Layout centerHeader;
		final Layout edit;

		final Layout pause;
		final Layout play;
		final Layout stop;

		final Layout rightColumn;
		final Layout rightFooter;

		final Layout properties;
		final Layout name;
		final Layout playerList;

		final Layout done;

		final Layout[] marginals;

		Layouts(Screen screen) {
			int fontHeight = screen.getMinecraft().fontRenderer.FONT_HEIGHT;

			Flex root = new Flex().columns();

			Flex header = root.child().rows()
					.width(1.0F, Flex.Unit.PERCENT).height(fontHeight).padding(PADDING)
					.alignMain(Align.Main.START);

			Flex body = root.child().rows()
					.width(1.0F, Flex.Unit.PERCENT).grow(1.0F);

			Flex leftColumn = body.child()
					.size(0.25F, 1.0F, Flex.Unit.PERCENT)
					.alignMain(Align.Main.START);

			Flex gameList = leftColumn.child()
					.width(1.0F, Flex.Unit.PERCENT).grow(1.0F)
					.alignMain(Align.Main.START);

			Flex leftFooter = leftColumn.child()
					.width(1.0F, Flex.Unit.PERCENT).height(FOOTER_HEIGHT).padding(PADDING)
					.alignMain(Align.Main.END);

			Flex centerColumn = body.child().columns()
					.height(1.0F, Flex.Unit.PERCENT).grow(1.0F);

			Flex centerHeader = centerColumn.child()
					.width(1.0F, Flex.Unit.PERCENT).height(fontHeight).padding(3)
					.alignMain(Align.Main.START);

			Flex edit = centerColumn.child()
					.width(1.0F, Flex.Unit.PERCENT).grow(1.0F)
					.padding(PADDING);

			Flex centerFooter = centerColumn.child().columns()
					.width(1.0F, Flex.Unit.PERCENT).height(FOOTER_HEIGHT).padding(PADDING)
					.alignMain(Align.Main.END);

			Flex controls = centerFooter.child().rows()
					.alignCross(Align.Cross.CENTER);

			Flex pause = controls.child().size(20, 20).margin(2, 0);
			Flex play = controls.child().size(20, 20).margin(2, 0);
			Flex stop = controls.child().size(20, 20).margin(2, 0);

			Flex rightColumn = body.child().columns()
					.size(0.25F, 1.0F, Flex.Unit.PERCENT)
					.alignMain(Align.Main.END);

			Flex properties = rightColumn.child().columns()
					.width(1.0F, Flex.Unit.PERCENT).grow(1.0F).padding(PADDING);

			Flex name = properties.child()
					.width(1.0F, Flex.Unit.PERCENT).height(20)
					.margin(2).marginTop(fontHeight);

			Flex playerList = properties.child()
					.width(1.0F, Flex.Unit.PERCENT).grow(1.0F)
					.marginTop(PADDING);

			Flex rightFooter = rightColumn.child()
					.width(1.0F, Flex.Unit.PERCENT).height(FOOTER_HEIGHT).padding(PADDING)
					.alignMain(Align.Main.END);

			Flex done = rightFooter.child()
					.width(1.0F, Flex.Unit.PERCENT).height(20)
					.alignMain(Align.Main.END);

			FlexSolver.Results solve = new FlexSolver(new Box(screen)).apply(root);

			this.header = solve.layout(header);

			this.leftColumn = solve.layout(leftColumn);
			this.leftFooter = solve.layout(leftFooter);
			this.gameList = solve.layout(gameList);

			this.centerColumn = solve.layout(centerColumn);
			this.centerHeader = solve.layout(centerHeader);
			this.centerFooter = solve.layout(centerFooter);
			this.edit = solve.layout(edit);

			this.pause = solve.layout(pause);
			this.play = solve.layout(play);
			this.stop = solve.layout(stop);

			this.rightColumn = solve.layout(rightColumn);
			this.rightFooter = solve.layout(rightFooter);
			this.properties = solve.layout(properties);
			this.name = solve.layout(name);
			this.playerList = solve.layout(playerList);
			this.done = solve.layout(done);

			this.marginals = new Layout[] { this.header, this.leftFooter, this.centerFooter, this.rightFooter };
		}
	}
}
