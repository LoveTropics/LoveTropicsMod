package com.lovetropics.minigames.common.core.game.map;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public record RandomMapProvider(IGameMapProvider[] mapProviders) implements IGameMapProvider {
	public static final Codec<RandomMapProvider> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.arrayOrUnit(GameMapProviders.CODEC, IGameMapProvider[]::new).fieldOf("pool").forGetter(c -> c.mapProviders)
	).apply(i, RandomMapProvider::new));

	private static final RandomSource RANDOM = RandomSource.create();

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
