package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import net.minecraft.server.MinecraftServer;

public interface IGamePhase extends IProtoGame {
	IGameInstance getInstance();

	@Override
	default GameInstanceId getInstanceId() {
		return getInstance().getInstanceId();
	}

	@Override
	default IGameDefinition getDefinition() {
		return getInstance().getDefinition();
	}

	@Override
	default MinecraftServer getServer() {
		return getInstance().getServer();
	}

	@Override
	default PlayerKey getInitiator() {
		return getInstance().getInitiator();
	}
}
