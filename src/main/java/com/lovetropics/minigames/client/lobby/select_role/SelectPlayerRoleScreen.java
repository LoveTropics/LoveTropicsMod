package com.lovetropics.minigames.client.lobby.select_role;

import com.lovetropics.minigames.client.screen.FlexUi;
import com.lovetropics.minigames.client.screen.flex.*;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public final class SelectPlayerRoleScreen extends Screen {
	private static final ITextComponent TITLE = GameTexts.Ui.selectPlayerRole()
			.mergeStyle(TextFormatting.BOLD, TextFormatting.UNDERLINE);

	// TODO: translate all the things
	private static final ITextComponent[] TEXT = new ITextComponent[] {
			new StringTextComponent("Welcome to the game lobby!"),
			new StringTextComponent("Before the game, ")
					.appendSibling(new StringTextComponent("please select to ")
							.appendSibling(new StringTextComponent("play").mergeStyle(TextFormatting.AQUA))
							.appendString(" or ")
							.appendSibling(new StringTextComponent("spectate").mergeStyle(TextFormatting.AQUA))
							.mergeStyle(TextFormatting.UNDERLINE)
					)
					.appendString("."),
			new StringTextComponent("You will be prompted before each game in this lobby.").mergeStyle(TextFormatting.GRAY)
	};

	private static final int PADDING = 4;

	private final int lobbyId;

	private ScreenLayout layout;
	private boolean responded;

	public SelectPlayerRoleScreen(int lobbyId) {
		super(TITLE);
		this.lobbyId = lobbyId;
	}

	@Override
	protected void init() {
		super.init();

		layout = new ScreenLayout(this);

		this.addButton(FlexUi.createButton(layout.play, new StringTextComponent("Play"), b -> {
			this.acceptResponse(true);
			this.closeScreen();
		}));
		this.addButton(FlexUi.createButton(layout.spectate, new StringTextComponent("Spectate"), b -> {
			this.acceptResponse(false);
			this.closeScreen();
		}));
	}

	private void acceptResponse(boolean play) {
		if (!this.responded) {
			LoveTropicsNetwork.CHANNEL.sendToServer(new SelectRoleMessage(lobbyId, play));
			this.responded = true;
		}
	}

	@Override
	public void onClose() {
		super.onClose();
		this.acceptResponse(false);
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);

		super.render(matrixStack, mouseX, mouseY, partialTicks);

		this.renderText(matrixStack);
	}

	private void renderText(MatrixStack matrixStack) {
		Box box = layout.text.content();

		int lineHeight = font.FONT_HEIGHT + PADDING;
		int y = box.bottom() - lineHeight * TEXT.length;

		for (ITextComponent line : TEXT) {
			int length = font.getStringPropertyWidth(line);
			int x = box.centerX() - length / 2;

			font.drawText(matrixStack, line, x, y, 0xFFFFFFFF);

			y += lineHeight;
		}
	}

	static final class ScreenLayout {
		static final int BUTTON_WIDTH = 100;

		final Layout text;
		final Layout play;
		final Layout spectate;

		ScreenLayout(Screen screen) {
			Flex root = new Flex().column();

			Flex text = root.child().width(1.0F, Flex.Unit.PERCENT)
					.grow(1.0F);

			Flex buttons = root.child().row().width(1.0F, Flex.Unit.PERCENT).grow(1.0F);

			Flex left = buttons.child().row().grow(1.0F).height(1.0F, Flex.Unit.PERCENT);
			Flex right = buttons.child().row().grow(1.0F).height(1.0F, Flex.Unit.PERCENT);

			Flex play = left.child()
					.alignMain(Align.Main.END)
					.margin(PADDING)
					.size(BUTTON_WIDTH, 20);

			Flex spectate = right.child()
					.alignMain(Align.Main.START)
					.margin(PADDING)
					.size(BUTTON_WIDTH, 20);

			FlexSolver.Results solve = new FlexSolver(new Box(screen)).apply(root);

			this.text = solve.layout(text);
			this.play = solve.layout(play);
			this.spectate = solve.layout(spectate);
		}
	}
}
