package com.lovetropics.minigames.client.lobby.manage.screen.game_list;

import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyManageState;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.screen.FlexUi;
import com.lovetropics.minigames.client.screen.flex.Flex;
import com.lovetropics.minigames.client.screen.flex.FlexSolver;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntConsumer;

public final class InstalledGameList extends AbstractGameList {
	private static final Component TITLE = GameTexts.Ui.INSTALLED_GAMES.copy()
			.withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);

	private final ClientLobbyManageState lobby;
	private final IntConsumer select;

	private final Button enqueueButton;
	private final Button cancelButton;

	public InstalledGameList(Screen screen, Layout main, Layout footer, ClientLobbyManageState lobby, IntConsumer select) {
		super(screen, main, TITLE);
		this.lobby = lobby;
		this.select = select;

		Flex root = new Flex().row();
		Flex enqueue = root.child().size(20, 20).marginRight(2);
		Flex cancel = root.child().size(20, 20).marginLeft(2);

		FlexSolver.Results solve = new FlexSolver(footer.content()).apply(root);
		this.enqueueButton = FlexUi.createButton(solve.layout(enqueue), Component.literal("✔"), this::enqueue);
		this.cancelButton = FlexUi.createButton(solve.layout(cancel), Component.literal("❌"), this::cancel);
	}

	@Override
	public void updateEntries() {
		this.setSelected(null);

		List<ClientGameDefinition> games = this.lobby.getInstalledGames();

		this.clearEntries();
		for (int id = 0; id < games.size(); id++) {
			ClientGameDefinition game = games.get(id);
			this.addEntry(Entry.game(this, id, game));
		}
	}

	private void enqueue(Button button) {
		Entry selected = this.getSelected();
		this.select.accept(selected != null ? selected.getId() : -1);
	}

	private void cancel(Button button) {
		this.select.accept(-1);
	}

	@Override
	public void renderOverlays(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		super.renderOverlays(graphics, mouseX, mouseY, partialTicks);
		this.enqueueButton.render(graphics, mouseX, mouseY, partialTicks);
		this.cancelButton.render(graphics, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (this.enqueueButton.mouseClicked(mouseX, mouseY, button) || this.cancelButton.mouseClicked(mouseX, mouseY, button)) {
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void setSelected(@Nullable Entry entry) {
		super.setSelected(entry);
		this.enqueueButton.active = entry != null;
	}
}
