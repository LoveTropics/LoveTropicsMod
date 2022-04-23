package com.lovetropics.minigames.common.core.game.map;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensionConfig;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensionHandle;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensions;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.map.*;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.IResource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class LoadMapProvider implements IGameMapProvider {
	public static final Codec<LoadMapProvider> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.optionalFieldOf("name").forGetter(c -> Optional.ofNullable(c.name)),
				ResourceLocation.CODEC.fieldOf("load_from").forGetter(c -> c.loadFrom),
				DimensionType.CODEC.optionalFieldOf("dimension_type").forGetter(c -> Optional.ofNullable(c.dimensionType)),
				ResourceLocation.CODEC.optionalFieldOf("dimension").forGetter(c -> Optional.ofNullable(c.dimension))
		).apply(instance, LoadMapProvider::new);
	});

	private static final Logger LOGGER = LogManager.getLogger(LoadMapProvider.class);

	private final String name;
	private final ResourceLocation loadFrom;
	private final Supplier<DimensionType> dimensionType;
	private final ResourceLocation dimension;

	public LoadMapProvider(final Optional<String> name, final ResourceLocation loadFrom, Optional<Supplier<DimensionType>> dimensionType, Optional<ResourceLocation> dimension) {
		this.name = name.orElse(null);
		this.loadFrom = loadFrom;
		this.dimensionType = dimensionType.orElse(null);
		this.dimension = dimension.orElse(null);
	}

	@Override
	public Codec<? extends IGameMapProvider> getCodec() {
		return CODEC;
	}

	@Override
	public List<RegistryKey<World>> getPossibleDimensions() {
		if (dimension != null) {
			return ImmutableList.of(RegistryKey.create(Registry.DIMENSION_REGISTRY, dimension));
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public CompletableFuture<GameResult<GameMap>> open(MinecraftServer server) {
		Supplier<DimensionType> dimensionType = this.dimensionType != null ? this.dimensionType : () -> server.overworld().dimensionType();
		Dimension dimension = new Dimension(dimensionType, new VoidChunkGenerator(server));
		MapWorldSettings worldSettings = new MapWorldSettings();

		MapWorldInfo worldInfo = MapWorldInfo.create(server, worldSettings);
		RuntimeDimensionConfig config = new RuntimeDimensionConfig(dimension, 0, worldInfo);

		return CompletableFuture.supplyAsync(() -> this.openDimension(server, config), server)
				.thenApplyAsync(result -> result.andThen(handle -> {
					return this.loadMapInto(server, worldSettings, handle);
				}), Util.backgroundExecutor())
				.thenApplyAsync(result -> result.map(pair -> {
					RuntimeDimensionHandle dimensionHandle = pair.getFirst();
					MapMetadata metadata = pair.getSecond();
					return new GameMap(name, dimensionHandle.asKey(), metadata.regions)
							.onClose(game -> dimensionHandle.delete());
				}), server);
	}

	private GameResult<RuntimeDimensionHandle> openDimension(MinecraftServer server, RuntimeDimensionConfig config) {
		RuntimeDimensions dimensions = RuntimeDimensions.get(server);
		if (this.dimension == null) {
			return GameResult.ok(dimensions.openTemporary(config));
		}

		RuntimeDimensionHandle handle = dimensions.openTemporaryWithKey(this.dimension, config);
		if (handle != null) {
			return GameResult.ok(handle);
		} else {
			return GameResult.error(new StringTextComponent("Dimension already loaded in '" + this.dimension + "'"));
		}
	}

	private GameResult<Pair<RuntimeDimensionHandle, MapMetadata>> loadMapInto(MinecraftServer server, MapWorldSettings mapWorldSettings, RuntimeDimensionHandle handle) {
		ResourceLocation path = new ResourceLocation(loadFrom.getNamespace(), "maps/" + loadFrom.getPath() + ".zip");

		try (IResource resource = server.getDataPackRegistries().getResourceManager().getResource(path)) {
			try (MapExportReader reader = MapExportReader.open(resource.getInputStream())) {
				MapMetadata metadata = reader.loadInto(server, handle.asKey());
				mapWorldSettings.importFrom(metadata.settings);
				return GameResult.ok(Pair.of(handle, metadata));
			}
		} catch (IOException e) {
			LOGGER.error("Failed to load map from {}", path, e);
			return GameResult.fromException("Failed to load map from '" + path + "'", e);
		}
	}
}
