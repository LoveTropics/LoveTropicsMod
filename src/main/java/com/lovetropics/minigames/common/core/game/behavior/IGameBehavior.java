package com.lovetropics.minigames.common.core.game.behavior;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.polling.PollingGameInstance;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public interface IGameBehavior {
	Codec<IGameBehavior> CODEC = GameBehaviorTypes.TYPE_CODEC.partialDispatch(
			"type",
			behavior -> DataResult.error("Encoding unsupported"), // very sad.
			type -> DataResult.success(type.codec)
	);

	/**
	 * Called before the game starts. This should be used to register all event listeners and do any early setup.
	 *
	 * @param registerGame The game that is being constructed
	 * @param events The event listeners to register to
	 *
	 * @throws GameException if this behavior was not able to be initialized
	 */
	void register(IGameInstance registerGame, EventRegistrar events) throws GameException;

	/**
	 * Called before the game starts polling. This should be used to register all event listeners and do any early setup.
	 *
	 * @param registerGame The game that is being constructed
	 * @param events The event listeners to register to
	 *
	 * @throws GameException if this behavior was not able to be initialized
	 */
	default void registerPolling(PollingGameInstance registerGame, EventRegistrar events) throws GameException {
	}

	default void registerState(GameStateMap state) {
	}
}
