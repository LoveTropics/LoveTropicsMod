package com.lovetropics.minigames.common.minigames.map;

import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameMap;
import com.lovetropics.minigames.common.minigames.MinigameResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;

import java.util.concurrent.CompletableFuture;

public interface IMinigameMapProvider {
	static <T> IMinigameMapProvider parse(Dynamic<T> root) {
		ResourceLocation mapProviderId = new ResourceLocation(root.get("type").asString(""));
		MinigameMapProviderType mapProviderType = MinigameMapProviderTypes.REGISTRY.get().getValue(mapProviderId);
		if (mapProviderType == null) {
			throw new RuntimeException("invalid map provider with id '" + mapProviderId + "'");
		}

		return mapProviderType.create(root);
	}

	MinigameResult<Unit> canOpen(IMinigameDefinition definition, MinecraftServer server);

	CompletableFuture<MinigameMap> open(MinecraftServer server);

	void close(IMinigameInstance minigame);
}
