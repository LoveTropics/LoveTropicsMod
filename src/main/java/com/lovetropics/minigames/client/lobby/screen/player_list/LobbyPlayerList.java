package com.lovetropics.minigames.client.lobby.screen.player_list;

import com.lovetropics.minigames.client.screen.PlayerFaces;
import com.lovetropics.minigames.client.screen.flex.Box;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.IGuiEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class LobbyPlayerList extends AbstractGui implements IGuiEventListener {
	private static final int FACE_SIZE = 16;
	private static final int SPACING = 4;

	private static final int BLOCK_SIZE = FACE_SIZE + SPACING;

	private final int rows;
	private final int columns;

	private final Box layout;

	public LobbyPlayerList(Layout layout) {
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

	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		// TODO: use actual player list (and group by spectator/participant?)
		List<UUID> players = new ArrayList<>();
		for (AbstractClientPlayerEntity player : Minecraft.getInstance().world.getPlayers()) {
			players.add(player.getUniqueID());
		}

		// TODO: handling overflow with scrollbar
		for (int i = 0; i < players.size(); i++) {
			UUID player = players.get(i);

			int x = this.faceX(i % rows);
			int y = this.faceY(i / rows);

			boolean hovered = mouseX >= x - 1 && mouseY >= y - 1 && mouseX < x + FACE_SIZE + 1 && mouseY < y + FACE_SIZE + 1;

			fill(matrixStack,
					x - 1, y - 1,
					x + FACE_SIZE + 1, y + FACE_SIZE + 1,
					hovered ? 0xFFF0F0F0 : 0xFF000000
			);
			PlayerFaces.render(player, matrixStack, x, y, FACE_SIZE);
		}
	}

	private int faceX(int row) {
		return layout.left() + row * BLOCK_SIZE;
	}

	private int faceY(int column) {
		return layout.top() + column * BLOCK_SIZE;
	}
}
