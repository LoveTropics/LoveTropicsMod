package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.player.PlayerIterable;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public interface IGameLobby {
	MinecraftServer getServer();

	GameLobbyMetadata getMetadata();

	IGameLobbyPlayers getPlayers();

	LobbyGameQueue getGameQueue();

	@Nullable
	IGameInstance getCurrentGame();

	@Nullable
	default IGamePhase getCurrentPhase() {
		IGameInstance game = getCurrentGame();
		return game != null ? game.getCurrentPhase() : null;
	}

	LobbyControls getControls();

	ILobbyManagement getManagement();

	default PlayerIterable getTrackingPlayers() {
		return PlayerSet.ofServer(getServer()).filter(this::isVisibleTo);
	}

	default boolean isVisibleTo(CommandSource source) {
		return true;
	}

	default boolean isVisibleTo(ServerPlayerEntity player) {
		return this.isVisibleTo(player.getCommandSource());
	}
}
