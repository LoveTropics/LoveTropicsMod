package com.lovetropics.minigames.common.minigames.map;

import com.lovetropics.minigames.common.MoreCodecs;
import com.lovetropics.minigames.common.minigames.MinigameMap;
import com.lovetropics.minigames.common.minigames.MinigameResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

public final class RandomMapProvider implements IMinigameMapProvider {
	public static final Codec<RandomMapProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.arrayOrUnit(MinigameMapProviders.CODEC, IMinigameMapProvider[]::new).fieldOf("pool").forGetter(c -> c.mapProviders)
		).apply(instance, RandomMapProvider::new);
	});

	private static final Random RANDOM = new Random();

	private final IMinigameMapProvider[] mapProviders;

	public RandomMapProvider(IMinigameMapProvider[] mapProviders) {
		this.mapProviders = mapProviders;
	}

	@Override
	public Codec<? extends IMinigameMapProvider> getCodec() {
		return CODEC;
	}

	@Override
	public CompletableFuture<MinigameResult<MinigameMap>> open(MinecraftServer server) {
		IMinigameMapProvider map = mapProviders[RANDOM.nextInt(mapProviders.length)];
		return map.open(server);
	}
}
