package com.lovetropics.minigames.common.core.game.behavior;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.polling.PollingGameInstance;

public interface IGameBehavior {
	/**
	 * Called before the game starts. This should be used to register all event listeners and do any early setup.
	 *
	 * @param registerGame The game that is being constructed
	 * @param events The event listeners to register to
	 *
	 * @throws GameException if this behavior was not able to be initialized
	 */
	void register(IGameInstance registerGame, GameEventListeners events) throws GameException;

	/**
	 * Called before the game starts polling. This should be used to register all event listeners and do any early setup.
	 *
	 * @param registerGame The game that is being constructed
	 * @param events The event listeners to register to
	 *
	 * @throws GameException if this behavior was not able to be initialized
	 */
	default void registerPolling(PollingGameInstance registerGame, GameEventListeners events) throws GameException {
	}

	default ImmutableList<GameBehaviorType<?>> dependencies() {
		return ImmutableList.of();
	}
}
