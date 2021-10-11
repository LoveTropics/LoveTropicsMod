package com.lovetropics.minigames.common.core.game.config;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.resources.IResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.WorldSettingsImport;

import java.util.function.BiConsumer;

public final class BehaviorReference {
	private final GameBehaviorType<?> type;
	private final Dynamic<?> config;

	public BehaviorReference(GameBehaviorType<?> type, Dynamic<?> config) {
		this.type = type;
		this.config = config;
	}

	public void addTo(MinecraftServer server, BiConsumer<GameBehaviorType<?>, IGameBehavior> add) {
		DataResult<? extends IGameBehavior> result = parseWithWorldGen(server, type.codec, config);

		result.result().ifPresent(behavior -> add.accept(type, behavior));

		result.error().ifPresent(error -> {
			LoveTropics.LOGGER.warn("Failed to parse behavior declaration of type {}: {}", type.getRegistryName(), error);
		});
	}

	private static <T, B extends IGameBehavior> DataResult<B> parseWithWorldGen(MinecraftServer server, Codec<B> codec, Dynamic<T> config) {
		IResourceManager resourceManager = server.getDataPackRegistries().getResourceManager();
		DynamicRegistries.Impl dynamicRegistries = (DynamicRegistries.Impl) server.getDynamicRegistries();

		WorldSettingsImport<T> ops = WorldSettingsImport.create(config.getOps(), resourceManager, dynamicRegistries);
		return codec.parse(ops, config.getValue());
	}
}
