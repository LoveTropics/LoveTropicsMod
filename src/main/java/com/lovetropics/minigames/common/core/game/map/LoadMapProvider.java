package com.lovetropics.minigames.common.core.game.map;

import com.lovetropics.minigames.common.core.dimension.RuntimeDimensionConfig;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensionHandle;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensions;
import com.lovetropics.minigames.common.core.map.*;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.IResource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

public class LoadMapProvider implements IGameMapProvider {
	public static final Codec<LoadMapProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.optionalFieldOf("name").forGetter(c -> Optional.ofNullable(c.name)),
				ResourceLocation.CODEC.fieldOf("load_from").forGetter(c -> c.loadFrom),
				DimensionType.DIMENSION_TYPE_CODEC.optionalFieldOf("dimension").forGetter(c -> Optional.ofNullable(c.dimensionType))
		).apply(instance, LoadMapProvider::new);
	});

	private static final Logger LOGGER = LogManager.getLogger(LoadMapProvider.class);

	private final String name;
	private final ResourceLocation loadFrom;
	private final Supplier<DimensionType> dimensionType;

	public LoadMapProvider(final Optional<String> name, final ResourceLocation loadFrom, Optional<Supplier<DimensionType>> dimensionType) {
		this.name = name.orElse(null);
		this.loadFrom = loadFrom;
		this.dimensionType = dimensionType.orElse(null);
	}

	@Override
	public Codec<? extends IGameMapProvider> getCodec() {
		return CODEC;
	}

	@Override
	public CompletableFuture<GameResult<GameMap>> open(MinecraftServer server) {
		Supplier<DimensionType> dimensionType = this.dimensionType != null ? this.dimensionType : () -> server.func_241755_D_().getDimensionType();
		Dimension dimension = new Dimension(dimensionType, new VoidChunkGenerator(server));
		MapWorldSettings mapWorldSettings = new MapWorldSettings();

		MapWorldInfo worldInfo = MapWorldInfo.create(server, mapWorldSettings);

		return RuntimeDimensions.get(server).openTemporary(new RuntimeDimensionConfig(dimension, 0, worldInfo))
				.thenApplyAsync(dimensionHandle -> {
					ResourceLocation path = new ResourceLocation(loadFrom.getNamespace(), "maps/" + loadFrom.getPath() + ".zip");

					try (IResource resource = server.getDataPackRegistries().getResourceManager().getResource(path)) {
						try (MapExportReader reader = MapExportReader.open(resource.getInputStream())) {
							MapMetadata metadata = reader.loadInto(server, dimensionHandle.asKey());
							mapWorldSettings.importFrom(metadata.settings);
							return Pair.of(dimensionHandle, metadata);
						}
					} catch (IOException e) {
						LOGGER.error("Failed to load map from {}", path, e);
						throw new CompletionException(e);
					}
				}, Util.getServerExecutor())
				.thenApplyAsync(pair -> {
					RuntimeDimensionHandle dimensionHandle = pair.getFirst();
					MapMetadata metadata = pair.getSecond();

					GameMap map = new GameMap(name, dimensionHandle.asKey(), metadata.regions)
							.onClose(minigame -> dimensionHandle.delete());

					return GameResult.ok(map);
				}, server);
	}
}
