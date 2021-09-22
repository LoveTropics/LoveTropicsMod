package com.lovetropics.minigames.common.core.game.behavior;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public interface IGameBehavior {
	Codec<IGameBehavior> CODEC = GameBehaviorTypes.TYPE_CODEC.partialDispatch(
			"type",
			behavior -> DataResult.error("Encoding unsupported"), // very sad.
			type -> DataResult.success(type.codec)
	);

	default void registerState(GameStateMap state) {
	}

	/**
	 * Called before the game starts. This should be used to register all event listeners and do any early setup.
	 *
	 * @param registerGame The game that is being constructed
	 * @param events The event listeners to register to
	 * @throws GameException if this behavior was not able to be initialized
	 */
	void register(IActiveGame registerGame, EventRegistrar events) throws GameException;

	// TODO: not all games would want to have a polling phase- this might need to be explicitly requested
	//         this could possibly happen through the lobby configuration, or through a game config, or through specific
	//         behaviors requesting?
	/**
	 * Called before the game starts polling. This should be used to register all event listeners and do any early setup.
	 *
	 * @param registerGame The game that is being constructed
	 * @param events The event listeners to register to
	 * @throws GameException if this behavior was not able to be initialized
	 */
	default void registerWaiting(IGamePhase registerGame, EventRegistrar events) throws GameException {
	}
}
