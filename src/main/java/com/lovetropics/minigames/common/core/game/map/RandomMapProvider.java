package com.lovetropics.minigames.common.core.game.map;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class RandomMapProvider implements IGameMapProvider {
	public static final Codec<RandomMapProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.arrayOrUnit(GameMapProviders.CODEC, IGameMapProvider[]::new).fieldOf("pool").forGetter(c -> c.mapProviders)
		).apply(instance, RandomMapProvider::new);
	});

	private static final Random RANDOM = new Random();

	private final IGameMapProvider[] mapProviders;

	public RandomMapProvider(IGameMapProvider[] mapProviders) {
		this.mapProviders = mapProviders;
	}

	@Override
	public Codec<? extends IGameMapProvider> getCodec() {
		return CODEC;
	}

	@Override
	public List<ResourceKey<Level>> getPossibleDimensions() {
		return Arrays.stream(mapProviders)
				.map(IGameMapProvider::getPossibleDimensions)
				.flatMap(List::stream)
				.collect(Collectors.toList());
	}

	@Override
	public CompletableFuture<GameResult<GameMap>> open(MinecraftServer server) {
		IGameMapProvider map = mapProviders[RANDOM.nextInt(mapProviders.length)];
		return map.open(server);
	}
}
