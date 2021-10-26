package com.lovetropics.minigames.common.core.game.config;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.util.DynamicRegistryReadingOps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import net.minecraft.server.MinecraftServer;

import java.util.function.BiConsumer;

public final class BehaviorReference {
	private final GameBehaviorType<?> type;
	private final Dynamic<?> config;

	public BehaviorReference(GameBehaviorType<?> type, Dynamic<?> config) {
		this.type = type;
		this.config = config;
	}

	public void addTo(MinecraftServer server, BiConsumer<GameBehaviorType<?>, IGameBehavior> add) {
		DataResult<? extends IGameBehavior> result = parseWithDynamicRegistries(server, type.codec, config);
		result.result().ifPresent(behavior -> add.accept(type, behavior));

		result.error().ifPresent(error -> {
			LoveTropics.LOGGER.warn("Failed to parse behavior declaration of type {}: {}", type.getRegistryName(), error);
		});
	}

	private static <T, B extends IGameBehavior> DataResult<B> parseWithDynamicRegistries(MinecraftServer server, Codec<B> codec, Dynamic<T> config) {
		DynamicOps<T> ops = DynamicRegistryReadingOps.create(server, config.getOps());
		return codec.parse(ops, config.getValue());
	}
}
