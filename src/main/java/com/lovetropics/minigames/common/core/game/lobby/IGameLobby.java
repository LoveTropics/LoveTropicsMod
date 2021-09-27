package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import net.minecraft.command.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;

import javax.annotation.Nullable;

public interface IGameLobby {
	MinecraftServer getServer();

	GameLobbyMetadata getMetadata();

	IGameLobbyPlayers getPlayers();

	LobbyGameQueue getGameQueue();

	// TODO: merge into the game queue?
	@Nullable
	IGameInstance getCurrentGame();

	@Nullable
	default IGamePhase getCurrentPhase() {
		IGameInstance game = getCurrentGame();
		return game != null ? game.getCurrentPhase() : null;
	}

	GameResult<Unit> requestStart();

	default boolean isVisibleTo(CommandSource source) {
		return true;
	}
}
