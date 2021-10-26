package com.lovetropics.minigames.client.lobby;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyState;
import com.lovetropics.minigames.common.core.game.LobbyStatus;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Constants.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
public class LobbyStateGui {

	@SubscribeEvent
	public static void renderGameOverlay(RenderGameOverlayEvent event) {
		if (event.getType() != ElementType.TEXT && event.getType() != ElementType.HOTBAR) {
			return;
		}

		MatrixStack transform = event.getMatrixStack();
		FontRenderer fnt = Minecraft.getInstance().fontRenderer;

		final int padding = 2;
		final int lineHeight = fnt.FONT_HEIGHT + padding;
		int y = padding;

		for (ClientLobbyState lobby : ClientLobbyManager.getLobbies()) {
			// Nothing to show if they are currently playing an active minigame
			ClientCurrentGame currentGame = lobby.getCurrentGame();
			LobbyStatus status = lobby.getStatus();

			PlayerRole joinedRole = lobby.getJoinedRole();
			if (status == LobbyStatus.PLAYING && joinedRole != null) return;

			if (event.getType() == ElementType.HOTBAR) {
				Minecraft.getInstance().getTextureManager().bindTexture(new ResourceLocation("minecraft:missingno"));
			}
			if (event.getType() == ElementType.TEXT) {
				String line = TextFormatting.GRAY + "Lobby: " + TextFormatting.YELLOW + TextFormatting.BOLD + lobby.getName();
				if (joinedRole != null) {
					line += TextFormatting.GREEN + " [Joined]";
				}
				fnt.drawStringWithShadow(transform, line, padding, y, -1);
				y += lineHeight;

				String playerCount = formatPlayerCount(lobby, currentGame);

				line = TextFormatting.GRAY + "..."
						+ status.color + status.description
						+ TextFormatting.GRAY + " (" + playerCount + " players)";

				fnt.drawStringWithShadow(transform, line, padding, y, -1);
				y += lineHeight;

				line = TextFormatting.GRAY + "Commands: ";
				if (joinedRole == null) {
					if (status == LobbyStatus.WAITING) {
						line += TextFormatting.AQUA + "/join" + TextFormatting.GRAY + " or ";
					}
					line += TextFormatting.AQUA + "/spectate";
				} else {
					line += TextFormatting.AQUA + "/leave";
				}
				fnt.drawStringWithShadow(transform, line, padding, y, -1);
				y += lineHeight + padding;
			}
		}
	}

	private static String formatPlayerCount(ClientLobbyState lobby, ClientCurrentGame currentGame) {
		if (currentGame != null) {
			return lobby.getParticipantCount() + "/" + currentGame.definition().maximumParticipants;
		} else {
			return String.valueOf(lobby.getParticipantCount());
		}
	}
}
