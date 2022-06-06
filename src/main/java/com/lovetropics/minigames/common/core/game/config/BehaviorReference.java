package com.lovetropics.minigames.common.core.game.config;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;

import java.util.function.BiConsumer;

public record BehaviorReference(GameBehaviorType<?> type, Dynamic<?> config) {
	public void addTo(MinecraftServer server, BiConsumer<GameBehaviorType<?>, IGameBehavior> add) {
		DataResult<? extends IGameBehavior> result = parseWithDynamicRegistries(server, type.codec, config);
		result.result().ifPresent(behavior -> add.accept(type, behavior));

		result.error().ifPresent(error -> {
			LoveTropics.LOGGER.warn("Failed to parse behavior declaration of type {}: {}", type.getRegistryName(), error);
		});
	}

	private static <T, B extends IGameBehavior> DataResult<B> parseWithDynamicRegistries(MinecraftServer server, Codec<B> codec, Dynamic<T> config) {
		DynamicOps<T> ops = RegistryOps.create(config.getOps(), server.registryAccess());
		return codec.parse(ops, config.getValue());
	}
}
