package com.lovetropics.minigames.common.minigames.map;

import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameMap;
import com.lovetropics.minigames.common.minigames.MinigameResult;
import com.mojang.datafixers.Dynamic;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.world.dimension.DimensionType;

import java.util.concurrent.CompletableFuture;

public final class InlineMapProvider implements IMinigameMapProvider{
	private final DimensionType dimension;

	public InlineMapProvider(DimensionType dimension) {
		this.dimension = dimension;
	}

	public static <T> InlineMapProvider parse(Dynamic<T> root) {
		DimensionType dimension = DimensionType.byName(new ResourceLocation(root.get("dimension").asString("")));
		return new InlineMapProvider(dimension);
	}

	@Override
	public MinigameResult<Unit> canOpen(IMinigameDefinition definition, MinecraftServer server) {
		return MinigameResult.ok();
	}

	@Override
	public CompletableFuture<MinigameMap> open(MinecraftServer server) {
		return CompletableFuture.completedFuture(new MinigameMap(null, dimension));
	}

	@Override
	public void close(IMinigameInstance minigame) {
	}
}
