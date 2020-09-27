package com.lovetropics.minigames.common.minigames.map;

import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.mojang.datafixers.Dynamic;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.dimension.DimensionType;

import java.util.concurrent.CompletableFuture;

public interface IMinigameMapProvider {
	static <T> IMinigameMapProvider parse(Dynamic<T> root) {
		ResourceLocation mapProviderId = new ResourceLocation(root.get("type").asString(""));
		MinigameMapProviderType<?> mapProviderType = MinigameMapProviderTypes.REGISTRY.get().getValue(mapProviderId);
		if (mapProviderType == null) {
			throw new RuntimeException("invalid map provider with id '" + mapProviderId + "'");
		}

		return mapProviderType.create(root);
	}

	ActionResult<ITextComponent> canOpen(IMinigameDefinition definition, MinecraftServer server);

	CompletableFuture<DimensionType> open(IMinigameDefinition definition, MinecraftServer server, MapRegions regions);

	void close(IMinigameInstance minigame);
}
