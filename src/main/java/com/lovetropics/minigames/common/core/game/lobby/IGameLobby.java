package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.IGame;
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

	ILobbyGameQueue getGameQueue();

	@Nullable
	default IGame getCurrentGame() {
		IGamePhase phase = getCurrentPhase();
		return phase != null ? phase.getGame() : null;
	}

	@Nullable
	IGamePhase getCurrentPhase();

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

	default LobbyVisibility getVisibility() {
		return LobbyVisibility.PUBLIC;
	}
}
