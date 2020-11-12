package com.lovetropics.minigames.common.minigames;

import net.minecraft.server.MinecraftServer;

public interface ProtoMinigame {

	MinigameStatus getStatus();

	/**
	 * The definition used to define what content the minigame contains.
	 * @return The minigame definition.
	 */
	IMinigameDefinition getDefinition();

	MinecraftServer getServer();
}
