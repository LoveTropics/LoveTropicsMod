package com.lovetropics.minigames.common.minigames.map;

import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameMap;
import com.lovetropics.minigames.common.minigames.MinigameResult;
import com.mojang.serialization.Dynamic;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;

public final class InlineMapProvider implements IMinigameMapProvider{
	private final RegistryKey<World> dimension;

	public InlineMapProvider(RegistryKey<World> dimension) {
		this.dimension = dimension;
	}

	public static <T> InlineMapProvider parse(Dynamic<T> root) {
		RegistryKey<World> dimension = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(root.get("dimension").asString("")));
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
