package com.lovetropics.minigames.common.core.game.behavior;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigList;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.instances.CompositeBehavior;
import com.lovetropics.minigames.common.core.game.config.GameConfigs;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IGameBehavior {
	Codec<GameBehaviorType<?>> TYPE_CODEC = Codec.either(GameBehaviorTypes.TYPE_CODEC, GameConfigs.CUSTOM_BEHAVIORS)
			.xmap(e -> e.map(Function.identity(), Function.identity()), Either::left);

	MapCodec<IGameBehavior> MAP_CODEC = TYPE_CODEC.dispatchMap(behavior -> {throw new UnsupportedOperationException();}, type -> type.codec().codec());

	Codec<IGameBehavior> SIMPLE_CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<IGameBehavior, T>> decode(DynamicOps<T> ops, T input) {
			Optional<String> string = ops.getStringValue(input).result();
			if (string.isPresent()) {
				return TYPE_CODEC.parse(ops, input).flatMap(type -> parseTyped(ops, ops.emptyMap(), type, input));
			}
			return ops.get(input, "type")
					.flatMap(typeName -> TYPE_CODEC.parse(ops, typeName).map(type -> Pair.of(type, typeName)))
					.flatMap(type -> parseTyped(ops, ops.remove(input, "type"), type.getFirst(), type.getSecond()));
		}

		private static <T> DataResult<Pair<IGameBehavior, T>> parseTyped(DynamicOps<T> ops, T input, GameBehaviorType<?> type, T typeName) {
			return type.codec().codec().decode(ops, input)
					.mapError(err -> "In behavior " + typeName + ": " + err)
					.map(pair -> pair.mapFirst(b -> b));
		}

		@Override
		public <T> DataResult<T> encode(IGameBehavior input, DynamicOps<T> ops, T prefix) {
			return DataResult.error(() -> "Encoding unsupported");
		}
	};

	// Using custom codec for better error reporting
	Codec<IGameBehavior> CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<IGameBehavior, T>> decode(DynamicOps<T> ops, T input) {
			DataResult<Consumer<Consumer<T>>> list = ops.getList(input);
			if (list.result().isPresent()) {
				return CompositeBehavior.CODEC.decode(ops, input).map(p -> p.mapFirst(b -> b));
			} else {
				return SIMPLE_CODEC.decode(ops, input);
			}
		}

		@Override
		public <T> DataResult<T> encode(IGameBehavior input, DynamicOps<T> ops, T prefix) {
			return DataResult.error(() -> "Encoding unsupported");
		}
	};

	IGameBehavior EMPTY = new CompositeBehavior(List.of());

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
