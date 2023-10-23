package com.lovetropics.minigames.client.lobby.select_role;

import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public final class SelectPlayerRoleScreen extends Screen {
	private static final Component TITLE = GameTexts.Ui.SELECT_PLAYER_ROLE.copy()
			.withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE);
	private static final Component TEXT = GameTexts.Ui.selectRoleMessage(
			GameTexts.Ui.SELECT_PLAY.copy().withStyle(ChatFormatting.AQUA),
			GameTexts.Ui.SELECT_SPECTATE.copy().withStyle(ChatFormatting.AQUA)
	);

	private static final int BUTTON_WIDTH = 100;
	private static final int SPACING = 4;

	private final int lobbyId;

	private final GridLayout layout = new GridLayout().spacing(SPACING);
	private boolean responded;

	public SelectPlayerRoleScreen(int lobbyId) {
		super(TITLE);
		this.lobbyId = lobbyId;
	}

	@Override
	public Component getNarrationMessage() {
		return CommonComponents.joinForNarration(title, CommonComponents.joinForNarration(TEXT));
	}

	@Override
	protected void init() {
		GridLayout.RowHelper helper = layout.createRowHelper(1);
		layout.defaultCellSetting().alignHorizontallyCenter();

		helper.addChild(new MultiLineTextWidget(TEXT, font).setCentered(true));

		GridLayout buttons = helper.addChild(new GridLayout().spacing(SPACING), helper.newCellSettings().paddingTop(SPACING));
		GridLayout.RowHelper buttonsHelper = buttons.createRowHelper(2);
		buttonsHelper.addChild(Button.builder(GameTexts.Ui.SELECT_PLAY, b -> {
			sendResponse(true);
			onClose();
		}).width(BUTTON_WIDTH).build());
		buttonsHelper.addChild(Button.builder(GameTexts.Ui.SELECT_SPECTATE, b -> {
			sendResponse(false);
			onClose();
		}).width(BUTTON_WIDTH).build());

		layout.visitWidgets(this::addRenderableWidget);
		repositionElements();
	}

	@Override
	protected void repositionElements() {
		layout.arrangeElements();
		FrameLayout.centerInRectangle(layout, 0, 0, width, height);
	}

	private void sendResponse(boolean play) {
		if (!responded) {
			LoveTropicsNetwork.CHANNEL.sendToServer(new SelectRoleMessage(lobbyId, play));
			responded = true;
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		renderBackground(graphics);
		super.render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public void removed() {
		super.removed();
		sendResponse(false);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
}
