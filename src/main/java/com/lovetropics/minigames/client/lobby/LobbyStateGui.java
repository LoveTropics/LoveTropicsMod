package com.lovetropics.minigames.client.lobby;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyManager;
import com.lovetropics.minigames.client.lobby.state.ClientLobbyState;
import com.lovetropics.minigames.common.core.game.LobbyStatus;
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

import javax.annotation.Nullable;
import java.util.Collection;

@EventBusSubscriber(modid = Constants.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
public class LobbyStateGui {

	private static final int PADDING = 2;
	private static final int BOSS_BAR_WIDTH = 182;
	private static final int COMPACT_THRESHOLD = 200;

	private static boolean hasBossBar;

	@SubscribeEvent
	public static void onKeyInput(ClientTickEvent event) {
		if (event.phase == Phase.END) {
			if (LobbyKeybinds.JOIN.isPressed()) {
				Minecraft.getInstance().player.sendChatMessage("/game join");
			}
			if (LobbyKeybinds.LEAVE.isPressed()) {
				Minecraft.getInstance().player.sendChatMessage("/game leave");
			}
		}
	}

	@SubscribeEvent
	public static void renderGameOverlay(RenderGameOverlayEvent event) {
		if (event.getType() == ElementType.ALL) {
			hasBossBar = false;
		} else if (event.getType() == ElementType.BOSSINFO) {
			hasBossBar = true;
		}

		if (event.getType() != ElementType.TEXT && event.getType() != ElementType.HOTBAR) {
			return;
		}

		Minecraft client = Minecraft.getInstance();

		if (event.getType() == ElementType.HOTBAR) {
			client.getTextureManager().bindTexture(new ResourceLocation("minecraft:missingno"));
		}

		if (event.getType() == ElementType.TEXT) {
			ClientLobbyState joinedLobby = ClientLobbyManager.getJoined();
			Collection<ClientLobbyState> lobbies = ClientLobbyManager.getLobbies();
			if (lobbies.isEmpty()) return;

			renderLobbies(event, joinedLobby, lobbies);
		}
	}

	private static void renderLobbies(RenderGameOverlayEvent event, ClientLobbyState joinedLobby, Collection<ClientLobbyState> lobbies) {
		MatrixStack transform = event.getMatrixStack();

		if (joinedLobby != null) {
			if (joinedLobby.getStatus() != LobbyStatus.PLAYING) {
				render(transform, PADDING, PADDING, joinedLobby);
			}
		} else {
			int y = PADDING;
			for (ClientLobbyState lobby : lobbies) {
				y = render(transform, PADDING, y, lobby);
			}
		}
	}

	private static int render(MatrixStack transform, int left, int top, ClientLobbyState lobby) {
		Minecraft client = Minecraft.getInstance();

		final int iconSize = 32;
		final int lineHeight = client.fontRenderer.FONT_HEIGHT + PADDING;

		int x = left;

		ResourceLocation icon = getIcon(lobby);
		if (icon != null) {
			client.getTextureManager().bindTexture(icon);
			AbstractGui.blit(transform, x, top, 0, 0, 32, 32, 32, 32);
			x += iconSize + PADDING * 2;
		}

		if (shouldDisplayCompat(client, hasBossBar)) {
			return renderCompactText(transform, lineHeight, lobby, x, top);
		} else {
			return renderFullText(transform, lineHeight, lobby, x, top);
		}
	}

	private static int renderFullText(MatrixStack transform, int lineHeight, ClientLobbyState lobby, int left, int top) {
		Minecraft client = Minecraft.getInstance();

		LobbyStatus status = lobby.getStatus();
		ClientCurrentGame currentGame = lobby.getCurrentGame();
		boolean joined = lobby == ClientLobbyManager.getJoined();

		FontRenderer fnt = client.fontRenderer;

		int x = left;
		int y = top;

		String line = TextFormatting.GRAY + (currentGame != null ? "Game: " : "Lobby: ") + getLobbyName(lobby);
		if (joined) {
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

		line = TextFormatting.GRAY + keyBindsText(joined);
		fnt.drawStringWithShadow(transform, line, x, y, -1);
		y += lineHeight + PADDING;

		return y;
	}

	private static int renderCompactText(MatrixStack transform, int lineHeight, ClientLobbyState lobby, int left, int top) {
		Minecraft client = Minecraft.getInstance();

		boolean joined = lobby == ClientLobbyManager.getJoined();
		LobbyStatus status = lobby.getStatus();

		FontRenderer fnt = client.fontRenderer;

		int x = left;
		int y = top;

		String line = getLobbyName(lobby);

		fnt.drawStringWithShadow(transform, line, x, y, -1);
		y += lineHeight;

		line = TextFormatting.GRAY + "..." + lobby.getPlayerCount() + " "
				+ status.color + status.description
				+ TextFormatting.GRAY;

		fnt.drawStringWithShadow(transform, line, x, y, -1);
		y += lineHeight;

		line = keyBindsText(joined);

		fnt.drawStringWithShadow(transform, line, x, y, -1);
		y += lineHeight + PADDING;

		return y;
	}

	private static String getLobbyName(ClientLobbyState lobby) {
		ClientCurrentGame currentGame = lobby.getCurrentGame();
		if (currentGame != null) {
			String gameName = currentGame.definition().name.getString();
			return TextFormatting.YELLOW.toString() + TextFormatting.BOLD + gameName;
		} else {
			return TextFormatting.YELLOW.toString() + TextFormatting.BOLD + lobby.getName();
		}
	}

	@Nullable
	private static ResourceLocation getIcon(ClientLobbyState lobby) {
		ClientCurrentGame currentGame = lobby.getCurrentGame();
		if (currentGame != null) {
			ClientGameDefinition definition = currentGame.definition();
			if (definition.icon != null) {
				return definition.icon;
			}
		}

		return null;
	}

	private static String keyBindsText(boolean joined) {
		if (!joined) {
			return TextFormatting.AQUA + "Join [" + LobbyKeybinds.JOIN.func_238171_j_().getString().toUpperCase() + "]" + TextFormatting.GRAY;
		} else {
			return TextFormatting.AQUA + "Leave [" + LobbyKeybinds.LEAVE.func_238171_j_().getString().toUpperCase() + "]";
		}
	}

	private static boolean shouldDisplayCompat(Minecraft client, boolean bossBar) {
		if (bossBar) {
			int windowWidth = client.getMainWindow().getScaledWidth();

			int leftSpace = (windowWidth - BOSS_BAR_WIDTH) / 2;
			return leftSpace < COMPACT_THRESHOLD;
		} else {
			return false;
		}
	}

	private static String formatPlayerCount(ClientLobbyState lobby, ClientCurrentGame currentGame) {
		if (currentGame != null) {
			return lobby.getPlayerCount() + "/" + currentGame.definition().maximumParticipants;
		} else {
			return String.valueOf(lobby.getPlayerCount());
		}
	}
}
