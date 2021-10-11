package com.lovetropics.minigames.common.core.game.behavior;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
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

	default ConfigList getConfigurables() {
		return ConfigList.empty();
	}

//	default IGameBehavior withConfig(ConfigList config) {
//		return this;
//	}

	default void registerState(GameStateMap state) {
	}

	default void registerState(IGamePhase game, GameStateMap state) {
	}

	/**
	 * Called before the game starts. This should be used to register all event listeners and do any early setup.
	 *
	 * @param game The game that is being constructed
	 * @param events The event listeners to register to
	 * @throws GameException if this behavior was not able to be initialized
	 */
	void register(IGamePhase game, EventRegistrar events) throws GameException;
}
