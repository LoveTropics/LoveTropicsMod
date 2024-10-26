package com.lovetropics.minigames.client.lobby.manage.screen;

import com.lovetropics.minigames.client.lobby.manage.ClientLobbyManagement;
import com.lovetropics.minigames.client.screen.list.OrderableListEntry;
import com.lovetropics.minigames.client.screen.list.OrderableSelectionList;
import com.lovetropics.minigames.client.screen.tab.TabbedScreen;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.CommonLayouts;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ManageLobbyScreen extends TabbedScreen<ManageLobbyScreen.Tab> implements ClientLobbyManagement.Listener {
	private static final int FOOTER_WIDTH = Window.BASE_WIDTH - 10;

	private static final int CONTENT_WIDTH = 210;
	private static final int SPACING = 6;

	private final ClientLobbyManagement.Session session;

	private GamesList gamesList;

	public ManageLobbyScreen(ClientLobbyManagement.Session session) {
		super(GameTexts.Ui.MANAGE_GAME_LOBBY, List.of(Tab.values()));
		this.session = session;
	}

	@Override
	protected void populateLayout(HeaderAndFooterLayout layout, Tab tab) {
		switch (tab) {
			case GAMES -> gamesList = layout.addToContents(new GamesList(this));
			case LOBBY -> {
				LinearLayout content = layout.addToContents(LinearLayout.vertical().spacing(SPACING));

				content.addChild(createLabeledEditBox(CONTENT_WIDTH, Button.DEFAULT_HEIGHT, GameTexts.Ui.LOBBY_NAME));
				content.addChild(Button.builder(GameTexts.Ui.PUBLISH, b -> {}).width(CONTENT_WIDTH).build());
				content.addChild(Button.builder(GameTexts.Ui.CLOSE_LOBBY.copy().withStyle(ChatFormatting.RED), b -> closeLobby()).width(CONTENT_WIDTH).build());
			}
			case PLAYERS -> {
			}
		}

		// TODO: Make custom HeaderAndFooterLayout
		Layout footer = createFooter(tab);
		layout.addToFooter(footer, s -> s.padding(0, SPACING));
		footer.arrangeElements();
		layout.setFooterHeight(footer.getHeight() + SPACING * 2);
	}

	@Override
	protected void updateLayout(HeaderAndFooterLayout newLayout) {
		super.updateLayout(newLayout);
		gamesList.updateSize(width, newLayout);
	}

	private Layout createLabeledEditBox(int width, int height, Component label) {
		return CommonLayouts.labeledElement(font, new EditBox(font, 0, 0, width, height, label), label, s -> s.padding(1));
	}

	private Layout createFooter(Tab tab) {
		GridLayout footer = new GridLayout().spacing(SPACING);
		footer.defaultCellSetting().alignHorizontallyCenter();

		if (tab == Tab.GAMES) {
			int topButtonWidth = (FOOTER_WIDTH - SPACING) / 2;
			footer.addChild(Button.builder(Component.literal("Play"), b -> onClose()).width(topButtonWidth).build(), 0, 0, 1, 2).active = false;
			footer.addChild(Button.builder(Component.literal("Add"), b -> onClose()).width(topButtonWidth).build(), 0, 2, 1, 2);
			int bottomButtonWidth = (FOOTER_WIDTH - SPACING * 3) / 4;
			footer.addChild(Button.builder(Component.literal("Edit"), b -> onClose()).width(bottomButtonWidth).build(), 1, 0).active = false;
			footer.addChild(Button.builder(Component.literal("Remove"), b -> onClose()).width(bottomButtonWidth).build(), 1, 1).active = false;
			footer.addChild(Button.builder(Component.literal("Skip"), b -> onClose()).width(bottomButtonWidth).build(), 1, 2).active = false;
			footer.addChild(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).width(bottomButtonWidth).build(), 1, 3);
		} else {
			GridLayout.RowHelper helper = footer.createRowHelper(1);
			helper.addChild(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).build(), helper.newCellSettings());
		}

		return footer;
	}

	private void closeLobby() {
		session.closeLobby();
		onClose();
	}

	@Override
	public void removed() {
		super.removed();
		session.close();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void updateNameField() {

	}

	@Override
	public void updateGameEntries() {

	}

	@Override
	public void updateControlsState() {

	}

	@Override
	public void updatePublishState() {

	}

	public static class GamesList extends OrderableSelectionList<GamesList.Entry> {
		private static final int ENTRY_HEIGHT = 36;

		public GamesList(Screen screen) {
			super(screen, ENTRY_HEIGHT);
		}

		private static class Entry extends OrderableListEntry<Entry> {
			public Entry(OrderableSelectionList<Entry> list, Screen screen) {
				super(list, screen);
			}

			@Override
			public Component getNarration() {
				return CommonComponents.EMPTY;
			}

			@Override
			public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float partialTick) {

			}
		}
	}

	public enum Tab implements TabbedScreen.Tab {
		// TODO
		GAMES(Component.literal("Games")),
		LOBBY(Component.literal("Lobby")),
		PLAYERS(Component.literal("Players")),
		;

		private final Component title;

		Tab(Component title) {
			this.title = title;
		}

		@Override
		public Component title() {
			return title;
		}
	}
}
