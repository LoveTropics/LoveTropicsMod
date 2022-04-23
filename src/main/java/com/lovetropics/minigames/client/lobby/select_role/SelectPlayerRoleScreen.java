package com.lovetropics.minigames.client.lobby.select_role;

import com.lovetropics.minigames.client.screen.FlexUi;
import com.lovetropics.minigames.client.screen.flex.*;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

public final class SelectPlayerRoleScreen extends Screen {
	private static final Component TITLE = GameTexts.Ui.selectPlayerRole()
			.withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE);

	// TODO: translate all the things
	private static final Component[] TEXT = new Component[] {
			new TextComponent("Welcome to the game lobby!"),
			new TextComponent("Before the game, ")
					.append(new TextComponent("please select to ")
							.append(new TextComponent("play").withStyle(ChatFormatting.AQUA))
							.append(" or ")
							.append(new TextComponent("spectate").withStyle(ChatFormatting.AQUA))
							.withStyle(ChatFormatting.UNDERLINE)
					)
					.append("."),
			new TextComponent("You will be prompted before each game in this lobby.").withStyle(ChatFormatting.GRAY)
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

		this.addRenderableWidget(FlexUi.createButton(layout.play, new TextComponent("Play"), b -> {
			this.acceptResponse(true);
			this.onClose();
		}));
		this.addRenderableWidget(FlexUi.createButton(layout.spectate, new TextComponent("Spectate"), b -> {
			this.acceptResponse(false);
			this.onClose();
		}));
	}

	private void acceptResponse(boolean play) {
		if (!this.responded) {
			LoveTropicsNetwork.CHANNEL.sendToServer(new SelectRoleMessage(lobbyId, play));
			this.responded = true;
		}
	}

	@Override
	public void removed() {
		super.removed();
		this.acceptResponse(false);
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);

		super.render(matrixStack, mouseX, mouseY, partialTicks);

		this.renderText(matrixStack);
	}

	private void renderText(PoseStack matrixStack) {
		Box box = layout.text.content();

		int lineHeight = font.lineHeight + PADDING;
		int y = box.bottom() - lineHeight * TEXT.length;

		for (Component line : TEXT) {
			int length = font.width(line);
			int x = box.centerX() - length / 2;

			font.draw(matrixStack, line, x, y, 0xFFFFFFFF);

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
