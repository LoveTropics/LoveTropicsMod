package com.lovetropics.minigames.client.lobby.screen;

import com.lovetropics.minigames.client.screen.flex.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

// TODO: localisation
public final class ManageLobbyScreen extends Screen {
	private static final ITextComponent TITLE = new StringTextComponent("Manage Game Lobby");

	private Layouts layouts;

	private TextFieldWidget lobbyName;
	private GameQueueList queueList;

	public ManageLobbyScreen() {
		super(TITLE);
	}

	@Override
	protected void init() {
		this.layouts = new Layouts(this);

		this.minecraft.keyboardListener.enableRepeatEvents(true);

		this.queueList = new GameQueueList(this, layouts.queue);
		this.children.add(this.queueList);

		this.queueList.addEntries();

		this.lobbyName = layouts.name.createTextField(font, new StringTextComponent("Lobby Name"));
		this.lobbyName.setMaxStringLength(128);
		this.lobbyName.setText("Lobby"); // TODO
		this.children.add(this.lobbyName);
		this.setFocusedDefault(this.lobbyName);

		this.addButton(layouts.done.createButton(DialogTexts.GUI_DONE, b -> this.closeScreen()));

		this.addButton(layouts.pause.createButton(new StringTextComponent("\u23F8"), b -> {}));
		this.addButton(layouts.play.createButton(new StringTextComponent("\u25B6"), b -> {}));
		this.addButton(layouts.stop.createButton(new StringTextComponent("\u23F9"), b -> {}));
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack, 0);

		this.queueList.render(matrixStack, mouseX, mouseY, partialTicks);

		fill(layouts.header.background(), matrixStack, 0xFF101010);
		fill(layouts.footer.background(), matrixStack, 0xFF101010);

		// TODO: make this name rendering better
		drawString(matrixStack, this.font, this.lobbyName.getMessage(), this.lobbyName.x, this.lobbyName.y - this.font.FONT_HEIGHT - 2, 0xFFFFFF);
		this.lobbyName.render(matrixStack, mouseX, mouseY, partialTicks);

		Box header = layouts.header.content();
		drawCenteredString(matrixStack, this.font, this.title, header.centerX(), header.centerY(), 0xFFFFFF);

		super.render(matrixStack, mouseX, mouseY, partialTicks);
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

		final Layout queue;

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
					.width(1.0F, Flex.Unit.PERCENT).grow(1.0F)
					.padding(PADDING);

			Flex queue = body.child().columns()
					.size(0.25F, 1.0F, Flex.Unit.PERCENT).padding(PADDING)
					.alignMain(Align.Main.START);

			Flex properties = body.child().columns()
					.size(0.25F, 1.0F, Flex.Unit.PERCENT).padding(PADDING)
					.alignMain(Align.Main.END);

			Flex name = properties.child()
					.width(1.0F, Flex.Unit.PERCENT).height(20).margin(PADDING);

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
			this.queue = solve.layout(queue);
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
