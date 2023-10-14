package com.lovetropics.minigames.common.core.game.behavior;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;

public interface IGameBehavior {
	Codec<IGameBehavior> CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<IGameBehavior, T>> decode(DynamicOps<T> ops, T input) {
			return ops.get(input, "type")
					.flatMap(type -> GameBehaviorTypes.TYPE_CODEC.parse(ops, type))
					.flatMap(type -> type.codec().decode(ops, ops.remove(input, "type")))
					.map(pair -> pair.mapFirst(b -> b));
		}

		@Override
		public <T> DataResult<T> encode(IGameBehavior input, DynamicOps<T> ops, T prefix) {
			return DataResult.error(() -> "Encoding unsupported");
		}
	};

	@Nullable
	default ConfigList getConfigurables() {
		return null;
	}

	default IGameBehavior configure(Map<ResourceLocation, ConfigList> configs) {
		return this;
	}

//	default IGameBehavior withConfig(ConfigList config) {
//		return this;
//	}

	default void registerState(IGamePhase game, GameStateMap phaseState, GameStateMap instanceState) {
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
