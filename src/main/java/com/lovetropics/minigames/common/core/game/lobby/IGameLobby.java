package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.common.core.game.IGame;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.player.PlayerIterable;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public interface IGameLobby {
	MinecraftServer getServer();

	GameLobbyMetadata getMetadata();

	IGameLobbyPlayers getPlayers();

	ILobbyGameQueue getGameQueue();

	@Nullable
	default IGame getCurrentGame() {
		IGamePhase phase = getActivePhase();
		return phase != null ? phase.game() : null;
	}

	@Nullable
	default IGameDefinition getCurrentGameDefinition() {
		IGamePhase phase = getActivePhase();
		return phase != null ? phase.definition() : null;
	}

	@Nullable
	IGamePhase getTopPhase();

	@Nullable
	IGamePhase getActivePhase();

	@Nullable
	default ClientCurrentGame getClientCurrentGame() {
		IGamePhase phase = getActivePhase();
		return phase != null ? ClientCurrentGame.create(phase) : null;
	}

	LobbyControls getControls();

	ILobbyManagement getManagement();

	default PlayerIterable getTrackingPlayers() {
		return PlayerSet.ofServer(getServer()).filter(this::isVisibleTo);
	}

	default boolean isVisibleTo(CommandSourceStack source) {
		return getMetadata().visibility().isPublic();
	}

	default boolean isVisibleTo(ServerPlayer player) {
		return isVisibleTo(player.createCommandSourceStack());
	}
}
