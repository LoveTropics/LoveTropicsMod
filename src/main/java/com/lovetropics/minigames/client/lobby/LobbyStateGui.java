package com.lovetropics.minigames.client.lobby;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyState;
import com.lovetropics.minigames.common.core.game.LobbyStatus;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Constants.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
public class LobbyStateGui {

	@SubscribeEvent
	public static void onKeyInput(ClientTickEvent event) {
		if (event.phase == Phase.END) {
			if (LobbyKeybinds.JOIN.isPressed()) {
				Minecraft.getInstance().player.sendChatMessage("/game join");
			}
			if (LobbyKeybinds.SPECTATE.isPressed()) {
				Minecraft.getInstance().player.sendChatMessage("/game spectate");
			}
			if (LobbyKeybinds.LEAVE.isPressed()) {
				Minecraft.getInstance().player.sendChatMessage("/game leave");
			}
		}
	}

	@SubscribeEvent
	public static void renderGameOverlay(RenderGameOverlayEvent event) {
		if (event.getType() != ElementType.TEXT && event.getType() != ElementType.HOTBAR) {
			return;
		}

		MatrixStack transform = event.getMatrixStack();
		FontRenderer fnt = Minecraft.getInstance().fontRenderer;

		final int padding = 2;
		final int lineHeight = fnt.FONT_HEIGHT + padding;

		final int left = padding;
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
				final int iconSize = 32;

				int x = left;

				if (currentGame != null) {
					ClientGameDefinition definition = currentGame.definition();
					if (definition.icon != null) {
						Minecraft.getInstance().getTextureManager().bindTexture(definition.icon);
						AbstractGui.blit(transform, x, y, 0, 0, 32, 32, 32, 32);
						x += iconSize + padding * 2;
					}
				}

				String line = TextFormatting.GRAY + "Lobby: " + TextFormatting.YELLOW + TextFormatting.BOLD + lobby.getName();
				if (joinedRole != null) {
					line += TextFormatting.GREEN + " [Joined]";
				}
				fnt.drawStringWithShadow(transform, line, x, y, -1);
				y += lineHeight;

				String playerCount = formatPlayerCount(lobby, currentGame);

				line = TextFormatting.GRAY + "..."
						+ status.color + status.description
						+ TextFormatting.GRAY + " (" + playerCount + " players)";

				fnt.drawStringWithShadow(transform, line, x, y, -1);
				y += lineHeight;

				line = TextFormatting.GRAY.toString();
				if (joinedRole == null) {
					// TODO is this right?
//					if (status == LobbyStatus.WAITING) {
						line += TextFormatting.AQUA + "Join [" + LobbyKeybinds.JOIN.func_238171_j_().getString().toUpperCase() + "]" + TextFormatting.GRAY + " or ";
//					}
					line += TextFormatting.AQUA + "Spectate [" + LobbyKeybinds.SPECTATE.func_238171_j_().getString().toUpperCase() + "]";
				} else {
					line += TextFormatting.AQUA + "Leave [" + LobbyKeybinds.LEAVE.func_238171_j_().getString().toUpperCase() + "]";
				}
				fnt.drawStringWithShadow(transform, line, x, y, -1);
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
