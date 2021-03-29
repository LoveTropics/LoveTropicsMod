package com.lovetropics.minigames.common.core.game;

import net.minecraft.server.MinecraftServer;

public interface ProtoGame {

	GameStatus getStatus();

	/**
	 * The definition used to define what content the minigame contains.
	 * @return The minigame definition.
	 */
	IGameDefinition getDefinition();

	MinecraftServer getServer();

	int getMemberCount(PlayerRole role);
}
