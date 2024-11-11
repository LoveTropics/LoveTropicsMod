package com.lovetropics.minigames.client.lobby;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyState;
import com.lovetropics.minigames.common.core.game.LobbyStatus;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import javax.annotation.Nullable;
import java.util.Collection;

@EventBusSubscriber(modid = LoveTropics.ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class LobbyStateGui {

	private static final int PADDING = 2;
	private static final int BOSS_BAR_WIDTH = 182;
	private static final int COMPACT_THRESHOLD = 200;

	private static boolean hasBossBar;

	@SubscribeEvent
	public static void onKeyInput(ClientTickEvent.Post event) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) {
			return;
		}
		if (LobbyKeybinds.JOIN.consumeClick()) {
			player.connection.sendUnsignedCommand("game join");
		}
		if (LobbyKeybinds.LEAVE.consumeClick()) {
			player.connection.sendUnsignedCommand("game leave");
		}
	}

	@SubscribeEvent
	public static void onRenderBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
		hasBossBar = true;
	}

	public static void registerOverlays(RegisterGuiLayersEvent event) {
		event.registerBelow(VanillaGuiLayers.DEBUG_OVERLAY, LoveTropics.location("minigame_lobby"), (graphics, deltaTracker) -> {
			ClientLobbyState joinedLobby = ClientLobbyManager.getJoined();
			Collection<ClientLobbyState> lobbies = ClientLobbyManager.getLobbies();
			if (!lobbies.isEmpty()) {
				renderLobbies(graphics, joinedLobby, lobbies, hasBossBar);
			}
			hasBossBar = false;
		});
	}

	private static void renderLobbies(GuiGraphics graphics, @Nullable ClientLobbyState joinedLobby, Collection<ClientLobbyState> lobbies, boolean hasBossBar) {
		if (joinedLobby != null) {
			if (joinedLobby.getStatus() != LobbyStatus.PLAYING) {
				render(graphics, PADDING, PADDING, joinedLobby, hasBossBar);
			}
		} else {
			int y = PADDING;
			for (ClientLobbyState lobby : lobbies) {
				y = render(graphics, PADDING, y, lobby, hasBossBar);
			}
		}
	}

	private static int render(GuiGraphics graphics, int left, int top, ClientLobbyState lobby, boolean hasBossBar) {
		Minecraft client = Minecraft.getInstance();

		final int iconSize = 32;
		final int lineHeight = client.font.lineHeight + PADDING;

		int x = left;

		ResourceLocation icon = getIcon(lobby);
		if (icon != null) {
			graphics.blit(icon, x, top, 0, 0, 32, 32, 32, 32);
			x += iconSize + PADDING * 2;
		}

		if (shouldDisplayCompact(client, hasBossBar)) {
			return renderCompactText(graphics, lineHeight, lobby, x, top);
		} else {
			return renderFullText(graphics, lineHeight, lobby, x, top);
		}
	}

	private static int renderFullText(GuiGraphics graphics, int lineHeight, ClientLobbyState lobby, int left, int top) {
		Minecraft client = Minecraft.getInstance();

		LobbyStatus status = lobby.getStatus();
		ClientCurrentGame currentGame = lobby.getCurrentGame();
		boolean joined = lobby == ClientLobbyManager.getJoined();

		Font fnt = client.font;

		int x = left;
		int y = top;

		String line = ChatFormatting.GRAY + (currentGame != null ? "Game: " : "Lobby: ") + getLobbyName(lobby);
		if (joined) {
			line += ChatFormatting.GREEN + " [Joined]";
		}

		graphics.drawString(fnt, line, x, y, CommonColors.WHITE);
		y += lineHeight;

		String playerCount = formatPlayerCount(lobby, currentGame);

		line = ChatFormatting.GRAY + "..."
				+ status.color + status.description
				+ ChatFormatting.GRAY + " (" + playerCount + " players)";

		graphics.drawString(fnt, line, x, y, CommonColors.WHITE);
		y += lineHeight;

		line = ChatFormatting.GRAY + keyBindsText(joined);
		graphics.drawString(fnt, line, x, y, CommonColors.WHITE);
		y += lineHeight + PADDING;

		return y;
	}

	private static int renderCompactText(GuiGraphics graphics, int lineHeight, ClientLobbyState lobby, int left, int top) {
		Minecraft client = Minecraft.getInstance();

		boolean joined = lobby == ClientLobbyManager.getJoined();
		LobbyStatus status = lobby.getStatus();

		Font fnt = client.font;

		int x = left;
		int y = top;

		String line = getLobbyName(lobby);

		graphics.drawString(fnt, line, x, y, CommonColors.WHITE);
		y += lineHeight;

		line = ChatFormatting.GRAY + "..." + lobby.getPlayerCount() + " "
				+ status.color + status.description
				+ ChatFormatting.GRAY;

		graphics.drawString(fnt, line, x, y, CommonColors.WHITE);
		y += lineHeight;

		line = keyBindsText(joined);

		graphics.drawString(fnt, line, x, y, CommonColors.WHITE);
		y += lineHeight + PADDING;

		return y;
	}

	private static String getLobbyName(ClientLobbyState lobby) {
		ClientCurrentGame currentGame = lobby.getCurrentGame();
		if (currentGame != null) {
			String gameName = currentGame.definition().name().getString();
			return ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD + gameName;
		} else {
			return ChatFormatting.YELLOW.toString() + ChatFormatting.BOLD + lobby.getName();
		}
	}

	@Nullable
	private static ResourceLocation getIcon(ClientLobbyState lobby) {
		ClientCurrentGame currentGame = lobby.getCurrentGame();
		if (currentGame != null) {
			ClientGameDefinition definition = currentGame.definition();
			if (definition.icon() != null) {
				return definition.icon();
			}
		}

		return null;
	}

	private static String keyBindsText(boolean joined) {
		if (!joined) {
			return ChatFormatting.AQUA + "Join [" + LobbyKeybinds.JOIN.getTranslatedKeyMessage().getString().toUpperCase() + "]" + ChatFormatting.GRAY;
		} else {
			return ChatFormatting.AQUA + "Leave [" + LobbyKeybinds.LEAVE.getTranslatedKeyMessage().getString().toUpperCase() + "]";
		}
	}

	private static boolean shouldDisplayCompact(Minecraft client, boolean bossBar) {
		if (bossBar) {
			int windowWidth = client.getWindow().getGuiScaledWidth();

			int leftSpace = (windowWidth - BOSS_BAR_WIDTH) / 2;
			return leftSpace < COMPACT_THRESHOLD;
		} else {
			return false;
		}
	}

	private static String formatPlayerCount(ClientLobbyState lobby, @Nullable ClientCurrentGame currentGame) {
		if (currentGame != null) {
			return lobby.getPlayerCount() + "/" + currentGame.definition().maximumParticipants();
		} else {
			return String.valueOf(lobby.getPlayerCount());
		}
	}
}
