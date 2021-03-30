package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import net.minecraft.server.MinecraftServer;

import java.util.Collection;

public interface ProtoGame {

	GameStatus getStatus();

	/**
	 * The definition used to define what content the minigame contains.
	 * @return The minigame definition.
	 */
	IGameDefinition getDefinition();

	MinecraftServer getServer();

	int getMemberCount(PlayerRole role);

	GameEventListeners events();

	Collection<IGameBehavior> getBehaviors();

	<T extends IGameBehavior> Collection<T> getBehaviors(GameBehaviorType<T> type);
}
