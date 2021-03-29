package com.lovetropics.minigames.common.core.game.map;

import com.lovetropics.minigames.common.util.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import java.util.concurrent.CompletableFuture;

public final class InlineMapProvider implements IGameMapProvider {
	public static final Codec<InlineMapProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.registryKey(Registry.WORLD_KEY).fieldOf("dimension").forGetter(c -> c.dimension)
		).apply(instance, InlineMapProvider::new);
	});

	private final RegistryKey<World> dimension;

	public InlineMapProvider(RegistryKey<World> dimension) {
		this.dimension = dimension;
	}

	@Override
	public Codec<? extends IGameMapProvider> getCodec() {
		return CODEC;
	}

	@Override
	public CompletableFuture<GameResult<GameMap>> open(MinecraftServer server) {
		if (server.getWorld(dimension) == null) {
			return CompletableFuture.completedFuture(GameResult.error(new StringTextComponent("Missing dimension " + dimension)));
		}

		GameMap map = new GameMap(null, dimension);
		return CompletableFuture.completedFuture(GameResult.ok(map));
	}
}
