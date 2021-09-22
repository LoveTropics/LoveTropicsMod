package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.instances.control.ControlCommandInvoker;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;

import javax.annotation.Nullable;

public interface IGameLobby {
	MinecraftServer getServer();

	GameLobbyId getId();

	PlayerKey getInitiator();

	PlayerSet getAllPlayers();

	LobbyGameQueue getGameQueue();

	@Nullable
	IActiveGame getActiveGame();

	ControlCommandInvoker getControlCommands();

	boolean registerPlayer(ServerPlayerEntity player, @Nullable PlayerRole requestedRole);

	boolean removePlayer(ServerPlayerEntity player);

	GameResult<Unit> requestStart();

	default LobbyVisibility getVisibility() {
		return LobbyVisibility.PUBLIC;
	}
}
