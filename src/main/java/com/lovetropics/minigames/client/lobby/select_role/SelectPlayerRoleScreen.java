package com.lovetropics.minigames.client.lobby.select_role;

import com.lovetropics.minigames.client.screen.FlexUi;
import com.lovetropics.minigames.client.screen.flex.*;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

// TODO: gegy thought dump:
//        we need to remove the lobby registrations system entirely, the lobby just needs to care about a list of players.
//        the behavior to open the ui should *ideally* be a behavior which we could throw into e.g. the waiting lobby
//        however, for games *without* a waiting lobby, this logic needs to happen outside of the game itself?
//         considering this, we may need to just open up the ui whenever a player is switching to a new game instance and not have it apart of the behavior.
//         we'll need to update role assignment logic and waiting lobby stuff to handle all of this.

// TODO: we also need to take care to ensure players can't get stuck outside of the UI without selecting (e.g. if another opens or somehow closes)
//        so we need some way to reopen it, or to cancel it being closed?
public final class SelectPlayerRoleScreen extends Screen {
	private static final ITextComponent TITLE = GameTexts.Ui.selectPlayerRole()
			.mergeStyle(TextFormatting.BOLD, TextFormatting.UNDERLINE);

	// TODO: translate all the things
	static final ITextComponent[] TEXT = new ITextComponent[] {
			new StringTextComponent("Welcome to the game lobby!"),
			new StringTextComponent("Before the next game starts, please select whether you want to ")
					.appendSibling(new StringTextComponent("play").mergeStyle(TextFormatting.AQUA))
					.appendString(" or ")
					.appendSibling(new StringTextComponent("spectate").mergeStyle(TextFormatting.AQUA))
	};

	static final int PADDING = 4;

	private ScreenLayout layout;

	public SelectPlayerRoleScreen() {
		super(TITLE);
	}

	@Override
	protected void init() {
		super.init();

		layout = new ScreenLayout(this);

		this.addButton(FlexUi.createButton(layout.play, new StringTextComponent("Play"), b -> {
			// TODO: do something
		}));

		this.addButton(FlexUi.createButton(layout.spectate, new StringTextComponent("Spectate"), b -> {

		}));
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);

		super.render(matrixStack, mouseX, mouseY, partialTicks);

		this.renderText(matrixStack);
	}

	private void renderText(MatrixStack matrixStack) {
		Box box = layout.text.content();

		int y = box.top();

		for (ITextComponent line : TEXT) {
			int length = font.getStringPropertyWidth(line);
			int x = box.centerX() - length / 2;

			font.drawText(matrixStack, line, x, y, 0xFFFFFFFF);

			y += font.FONT_HEIGHT + PADDING;
		}
	}

	static final class ScreenLayout {
		static final int BUTTON_WIDTH = 30;
		static final int HEADER_HEIGHT = 30;

		final Layout text;
		final Layout play;
		final Layout spectate;

		ScreenLayout(Screen screen) {
			int lineHeight = screen.getMinecraft().fontRenderer.FONT_HEIGHT;

			Flex root = new Flex().column();

			Flex header = root.child().width(1.0F, Flex.Unit.PERCENT).height(HEADER_HEIGHT);

			Flex text = root.child().width(1.0F, Flex.Unit.PERCENT)
					.height(TEXT.length * (lineHeight + PADDING));

			Flex buttons = root.child().row().width(1.0F, Flex.Unit.PERCENT).grow(1.0F);

			Flex left = buttons.child().row().grow(1.0F).height(1.0F, Flex.Unit.PERCENT);
			Flex right = buttons.child().row().grow(1.0F).height(1.0F, Flex.Unit.PERCENT);

			Flex play = left.child()
					.alignMain(Align.Main.END)
					.alignCross(Align.Cross.CENTER)
					.margin(PADDING)
					.size(BUTTON_WIDTH, 20);

			Flex spectate = right.child()
					.alignMain(Align.Main.START)
					.alignCross(Align.Cross.CENTER)
					.margin(PADDING)
					.size(BUTTON_WIDTH, 20);

			FlexSolver.Results solve = new FlexSolver(new Box(screen)).apply(root);

			this.text = solve.layout(text);
			this.play = solve.layout(play);
			this.spectate = solve.layout(spectate);
		}
	}
}
