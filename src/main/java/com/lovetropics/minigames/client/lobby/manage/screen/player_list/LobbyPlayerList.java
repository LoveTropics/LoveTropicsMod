package com.lovetropics.minigames.client.lobby.manage.screen.player_list;

import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyManageState;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyPlayer;
import com.lovetropics.minigames.client.screen.ClientPlayerInfo;
import com.lovetropics.minigames.client.screen.flex.Box;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

// TODO: grid element utility
public final class LobbyPlayerList implements GuiEventListener, NarratableEntry {
	private static final int FACE_SIZE = 16;
	private static final int SPACING = 4;
	private static final int HALF_SPACING = SPACING / 2;

	private static final int BLOCK_SIZE = FACE_SIZE + SPACING;

	private final Screen screen;
	private final ClientLobbyManageState lobby;

	private final int rows;
	private final int columns;

	private final Box layout;

	public LobbyPlayerList(Screen screen, ClientLobbyManageState lobby, Layout layout) {
		this.screen = screen;
		this.lobby = lobby;

		Box content = layout.content();
		this.rows = (content.width() + SPACING) / BLOCK_SIZE;
		this.columns = (content.height() + SPACING) / BLOCK_SIZE;

		int innerWidth = this.rows * BLOCK_SIZE - SPACING;
		int offsetX = (content.width() - innerWidth) / 2;

		this.layout = new Box(
				content.left() + offsetX, content.top(),
				content.right(), content.bottom()
		);
	}

	public void render(GuiGraphics graphics, int mouseX, int mouseY) {
		// TODO: handling overflow with scrollbar
		int i = 0;
		for (ClientLobbyPlayer player : lobby.getPlayers()) {
			int x = this.faceX(i % rows);
			int y = this.faceY(i / rows);
			this.renderFace(graphics, mouseX, mouseY, player, x, y);
			i++;
		}
	}

	private void renderFace(GuiGraphics graphics, int mouseX, int mouseY, ClientLobbyPlayer player, int x, int y) {
		boolean hovered = isFaceHovered(x, y, mouseX, mouseY);

		graphics.fill(
				x - 1, y - 1,
				x + FACE_SIZE + 1, y + FACE_SIZE + 1,
				hovered ? 0xFFF0F0F0 : 0xFF000000
		);
		PlayerFaceRenderer.draw(graphics, ClientPlayerInfo.getSkin(player.uuid()), x, y, FACE_SIZE);
	}

	public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
		int index = this.hoveredFaceAt(mouseX, mouseY);
		if (index != -1) {
			ClientLobbyPlayer player = lobby.getPlayers().get(index);

			Component name = ClientPlayerInfo.getName(player.uuid());
			if (name != null) {
				List<Component> tooltip = new ArrayList<>(2);
				tooltip.add(name);

				if (player.playingRole() != null) {
					tooltip.add(GameTexts.Ui.roleDescription(player.playingRole())
							.withStyle(ChatFormatting.GRAY));
				}

				graphics.renderComponentTooltip(screen.getMinecraft().font, tooltip, mouseX, mouseY);
			}
		}
	}

	private static boolean isFaceHovered(int x, int y, int mouseX, int mouseY) {
		return mouseX >= x - 1 && mouseY >= y - 1 && mouseX < x + FACE_SIZE + 1 && mouseY < y + FACE_SIZE + 1;
	}

	private int faceX(int row) {
		return layout.left() + row * BLOCK_SIZE;
	}

	private int faceY(int column) {
		return layout.top() + column * BLOCK_SIZE;
	}

	private int faceRow(int x) {
		return (x - layout.left() + HALF_SPACING) / BLOCK_SIZE;
	}

	private int faceColumn(int y) {
		return (y - layout.top() + HALF_SPACING) / BLOCK_SIZE;
	}

	private int hoveredFaceAt(int x, int y) {
		int row = faceRow(x);
		int column = faceColumn(y);
		return isFaceHovered(faceX(row), faceY(column), x, y) ? faceIndex(row, column) : -1;
	}

	private int faceIndex(int row, int column) {
		if (row < 0 || column < 0 || row > this.rows || column > this.columns) {
			return -1;
		}

		int index = row + column * this.rows;
		if (index >= 0 && index < lobby.getPlayers().size()) {
			return index;
		} else {
			return -1;
		}
	}

	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}

	@Override
	public void updateNarration(final NarrationElementOutput output) {
	}

	@Override
	public void setFocused(boolean pFocused) {
	}

	@Override
	public boolean isFocused() {
		return false;
	}
}
