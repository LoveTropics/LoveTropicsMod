package com.lovetropics.minigames.common.core.game.map;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public record InlineMapProvider(ResourceKey<Level> dimension) implements IGameMapProvider {
	public static final Codec<InlineMapProvider> CODEC = RecordCodecBuilder.create(i -> i.group(
			ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(c -> c.dimension)
	).apply(i, InlineMapProvider::new));

	@Override
	public Codec<? extends IGameMapProvider> getCodec() {
		return CODEC;
	}

	@Override
	public List<ResourceKey<Level>> getPossibleDimensions() {
		return Collections.singletonList(dimension);
	}

	@Override
	public CompletableFuture<GameResult<GameMap>> open(MinecraftServer server) {
		if (server.getLevel(dimension) == null) {
			return CompletableFuture.completedFuture(GameResult.error(Component.literal("Missing dimension " + dimension)));
		}

		GameMap map = new GameMap(null, dimension);
		return CompletableFuture.completedFuture(GameResult.ok(map));
	}
}
