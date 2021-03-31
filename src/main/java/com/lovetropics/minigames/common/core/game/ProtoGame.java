package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.control.GameControlCommands;
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

	BehaviorMap getBehaviors();

	GameEventListeners getEvents();

	default <T> T invoker(GameEventType<T> eventType) {
		return this.getEvents().invoker(eventType);
	}

	GameControlCommands getControlCommands();
}
