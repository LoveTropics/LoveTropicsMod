package com.lovetropics.minigames.client.lobby.screen;

import com.lovetropics.minigames.client.lobby.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.ClientGameQueueEntry;
import com.lovetropics.minigames.client.lobby.screen.game_list.GameList;
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
	private final List<ClientGameQueueEntry> queue;
	private final List<ClientGameDefinition> installedGames;

	private Layouts layouts;

	private TextFieldWidget nameField;
	private GameList gameList;

	private int selectedGameIndex = -1;

	public ManageLobbyScreen(String name, List<ClientGameQueueEntry> queue, List<ClientGameDefinition> installedGames) {
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

		gameList = addListener(new GameList(this, layouts.gameList, new GameList.Handlers() {
			@Override
			public void selectQueuedGame(int queuedGameIndex) {
				selectedGameIndex = queuedGameIndex;
			}

			@Override
			public void enqueueGame(int installedGameIndex) {
				if (installedGameIndex != -1) {
					queue.add(new ClientGameQueueEntry(installedGames.get(installedGameIndex)));
					gameList.setEntries(queue, installedGames);
				}
			}
		}));
		gameList.setEntries(queue, installedGames);

		nameField = addListener(layouts.name.createTextField(font, new StringTextComponent("Lobby Name")));
		nameField.setMaxStringLength(128);
		nameField.setText(name);
		setFocusedDefault(nameField);

		addButton(layouts.done.createButton(DialogTexts.GUI_DONE, b -> closeScreen()));

		addButton(layouts.pause.createButton(new StringTextComponent("\u23F8"), b -> {}));
		addButton(layouts.play.createButton(new StringTextComponent("\u25B6"), b -> {}));
		addButton(layouts.stop.createButton(new StringTextComponent("\u23F9"), b -> {}));
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		int fontHeight = font.FONT_HEIGHT;

		renderBackground(matrixStack, 0);

		fill(layouts.gameList.background(), matrixStack, 0x80101010);
		fill(layouts.properties.background(), matrixStack, 0x80101010);

		gameList.render(matrixStack, mouseX, mouseY, partialTicks);

		fill(layouts.header.background(), matrixStack, 0xFF101010);
		fill(layouts.footer.background(), matrixStack, 0xFF101010);

		// TODO: make this name rendering better
		drawString(matrixStack, font, nameField.getMessage(), nameField.x, nameField.y - fontHeight - 2, 0xFFFFFF);
		nameField.render(matrixStack, mouseX, mouseY, partialTicks);

		Box header = layouts.header.content();
		drawCenteredString(matrixStack, font, title, header.centerX(), header.centerY(), 0xFFFFFF);

		if (selectedGameIndex >= 0 && selectedGameIndex < queue.size()) {
			ClientGameQueueEntry selectedEntry = queue.get(selectedGameIndex);
			renderSelectedGame(selectedEntry, matrixStack, mouseX, mouseY, partialTicks);
		}

		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	private void renderSelectedGame(ClientGameQueueEntry entry, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		fill(layouts.editHeader.background(), matrixStack, 0x80101010);

		ITextComponent title = new StringTextComponent("")
				.appendSibling(new StringTextComponent("Managing: ").mergeStyle(TextFormatting.BOLD))
				.appendSibling(entry.definition.name);

		Box header = layouts.editHeader.content();
		drawCenteredString(matrixStack, font, title, header.centerX(), header.centerY() - font.FONT_HEIGHT / 2, 0xFFFFFF);
	}

	// TODO: extract this out
	private static void fill(Box box, MatrixStack matrixStack, int color) {
		fill(matrixStack, box.left(), box.top(), box.right(), box.bottom(), color);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public static final class Layouts {
		static final int PADDING = 8;

		final Layout header;

		final Layout gameList;

		final Layout editHeader;
		final Layout editBody;

		final Layout properties;
		final Layout name;

		final Layout footer;

		final Layout pause;
		final Layout play;
		final Layout stop;

		final Layout done;

		Layouts(Screen screen) {
			int fontHeight = screen.getMinecraft().fontRenderer.FONT_HEIGHT;

			Flex root = new Flex().columns();

			Flex header = root.child().rows()
					.width(1.0F, Flex.Unit.PERCENT).height(fontHeight).padding(PADDING)
					.alignMain(Align.Main.START);

			Flex body = root.child().rows()
					.width(1.0F, Flex.Unit.PERCENT).grow(1.0F);

			Flex gameList = body.child()
					.size(0.25F, 1.0F, Flex.Unit.PERCENT).padding(PADDING)
					.alignMain(Align.Main.START);

			Flex edit = body.child().columns()
					.height(1.0F, Flex.Unit.PERCENT).grow(1.0F);

			Flex editHeader = edit.child()
					.width(1.0F, Flex.Unit.PERCENT).height(fontHeight).padding(3)
					.alignMain(Align.Main.START);

			Flex editBody = edit.child()
					.height(1.0F, Flex.Unit.PERCENT).grow(1.0F)
					.padding(PADDING);

			Flex properties = body.child().columns()
					.size(0.25F, 1.0F, Flex.Unit.PERCENT).padding(PADDING)
					.alignMain(Align.Main.END);

			Flex name = properties.child()
					.width(1.0F, Flex.Unit.PERCENT).height(20)
					.margin(2).marginTop(fontHeight);

			Flex footer = root.child().rows()
					.width(1.0F, Flex.Unit.PERCENT).height(20).padding(PADDING)
					.alignMain(Align.Main.END);

			Flex controls = footer
					.child().columns().size(1.0F, 1.0F, Flex.Unit.PERCENT)
					.child().rows().alignCross(Align.Cross.CENTER);

			Flex done = footer.child()
					.size(100, 20)
					.alignMain(Align.Main.END);

			Flex pause = controls.child().size(20, 20).margin(2, 0);
			Flex play = controls.child().size(20, 20).margin(2, 0);
			Flex stop = controls.child().size(20, 20).margin(2, 0);

			FlexSolver.Results solve = new FlexSolver(screen.width, screen.height).apply(root);

			this.header = solve.layout(header);
			this.gameList = solve.layout(gameList);
			this.editHeader = solve.layout(editHeader);
			this.editBody = solve.layout(editBody);
			this.properties = solve.layout(properties);
			this.name = solve.layout(name);
			this.footer = solve.layout(footer);
			this.pause = solve.layout(pause);
			this.play = solve.layout(play);
			this.stop = solve.layout(stop);
			this.done = solve.layout(done);
		}
	}
}
