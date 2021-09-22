package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.instances.control.ControlCommandInvoker;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import net.minecraft.server.MinecraftServer;

// TODO: name? what should be the surface of this api?
public interface IGamePhase {
	IGameLobby getLobby();

	default MinecraftServer getServer() {
		return getLobby().getServer();
	}

	default PlayerKey getInitiator() {
		return getLobby().getMetadata().initiator();
	}

	default PlayerSet getAllPlayers() {
		return getLobby().getAllPlayers();
	}

	<T> T invoker(GameEventType<T> type);

	GameStateMap getState();

	default ControlCommandInvoker getControlCommands() {
		return ControlCommandInvoker.EMPTY;
	}
}
