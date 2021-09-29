package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.lobby.GameLobbyMetadata;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommandInvoker;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommands;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import net.minecraft.server.MinecraftServer;

public interface IGame {
	IGameLobby getLobby();

	default MinecraftServer getServer() {
		return getLobby().getServer();
	}

	default PlayerKey getInitiator() {
		return getLobby().getMetadata().initiator();
	}

	default PlayerSet getAllPlayers() {
		return getLobby().getPlayers();
	}

	IGameDefinition getDefinition();

	GameStateMap getState();

	default GameStatistics getStatistics() {
		return getState().get(GameStatistics.KEY);
	}

	default ControlCommands getControlCommands() {
		return getState().get(ControlCommands.KEY);
	}

	default ControlCommandInvoker getControlInvoker() {
		ControlCommands commands = getControlCommands();
		GameLobbyMetadata lobby = getLobby().getMetadata();
		return ControlCommandInvoker.create(commands, lobby);
	}
}
