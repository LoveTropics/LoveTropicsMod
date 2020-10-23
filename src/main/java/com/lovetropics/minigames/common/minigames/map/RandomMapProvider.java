package com.lovetropics.minigames.common.minigames.map;

import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameMap;
import com.lovetropics.minigames.common.minigames.MinigameResult;
import com.mojang.datafixers.Dynamic;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public final class RandomMapProvider implements IMinigameMapProvider {
	private static final Random RANDOM = new Random();

	private final IMinigameMapProvider[] mapProviders;
	private IMinigameMapProvider nextMapProvider;

	public RandomMapProvider(IMinigameMapProvider[] mapProviders) {
		this.mapProviders = mapProviders;
	}

	public static <T> RandomMapProvider parse(Dynamic<T> root) {
		IMinigameMapProvider[] mapProviders = root.get("pool").asList(IMinigameMapProvider::parse).toArray(new IMinigameMapProvider[0]);
		return new RandomMapProvider(mapProviders);
	}

	private IMinigameMapProvider selectNextMap() {
		return mapProviders[RANDOM.nextInt(mapProviders.length)];
	}

	@Override
	public MinigameResult<Unit> canOpen(IMinigameDefinition definition, MinecraftServer server) {
		nextMapProvider = selectNextMap();
		return nextMapProvider.canOpen(definition, server);
	}

	@Override
	public CompletableFuture<MinigameMap> open(MinecraftServer server) {
		return nextMapProvider.open(server);
	}

	@Override
	public void close(IMinigameInstance minigame) {
		nextMapProvider.close(minigame);
		nextMapProvider = null;
	}
}
