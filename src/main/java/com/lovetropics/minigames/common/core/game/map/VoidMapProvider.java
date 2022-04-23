package com.lovetropics.minigames.common.core.game.map;

import com.lovetropics.minigames.common.core.dimension.RuntimeDimensionConfig;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensionHandle;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensions;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.core.map.MapWorldInfo;
import com.lovetropics.minigames.common.core.map.MapWorldSettings;
import com.lovetropics.minigames.common.core.map.VoidChunkGenerator;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class VoidMapProvider implements IGameMapProvider {
	public static final Codec<VoidMapProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.optionalFieldOf("name").forGetter(c -> Optional.ofNullable(c.name)),
				DimensionType.CODEC.optionalFieldOf("dimension").forGetter(c -> Optional.ofNullable(c.dimensionType))
		).apply(instance, VoidMapProvider::new);
	});

	private final String name;
	private final Supplier<DimensionType> dimensionType;

	public VoidMapProvider(final Optional<String> name, Optional<Supplier<DimensionType>> dimensionType) {
		this.name = name.orElse(null);
		this.dimensionType = dimensionType.orElse(null);
	}

	@Override
	public Codec<? extends IGameMapProvider> getCodec() {
		return CODEC;
	}

	@Override
	public CompletableFuture<GameResult<GameMap>> open(MinecraftServer server) {
		Supplier<DimensionType> dimensionType = this.dimensionType != null ? this.dimensionType : () -> server.overworld().dimensionType();
		LevelStem dimension = new LevelStem(dimensionType, new VoidChunkGenerator(server));

		MapWorldInfo worldInfo = MapWorldInfo.create(server, new MapWorldSettings());
		RuntimeDimensionConfig config = new RuntimeDimensionConfig(dimension, 0, worldInfo);

		return CompletableFuture.supplyAsync(() -> {
			RuntimeDimensionHandle dimensionHandle = RuntimeDimensions.get(server).openTemporary(config);

			GameMap map = new GameMap(name, dimensionHandle.asKey(), new MapRegions())
					.onClose(game -> dimensionHandle.delete());

			return GameResult.ok(map);
		}, server);
	}
}
