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
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record LoadMapProvider(
		Optional<String> name,
		ResourceLocation loadFrom,
		Optional<Holder<DimensionType>> dimensionType,
		Optional<ResourceLocation> dimension
) implements IGameMapProvider {
	public static final Codec<LoadMapProvider> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.optionalFieldOf("name").forGetter(c -> c.name),
			ResourceLocation.CODEC.fieldOf("load_from").forGetter(c -> c.loadFrom),
			DimensionType.CODEC.optionalFieldOf("dimension_type").forGetter(c -> c.dimensionType),
			ResourceLocation.CODEC.optionalFieldOf("dimension").forGetter(c -> c.dimension)
	).apply(i, LoadMapProvider::new));

	private static final Logger LOGGER = LogManager.getLogger(LoadMapProvider.class);

	@Override
	public Codec<? extends IGameMapProvider> getCodec() {
		return CODEC;
	}

	@Override
	public List<ResourceKey<Level>> getPossibleDimensions() {
		if (dimension.isPresent()) {
			return ImmutableList.of(ResourceKey.create(Registry.DIMENSION_REGISTRY, dimension.get()));
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public CompletableFuture<GameResult<GameMap>> open(MinecraftServer server) {
		Holder<DimensionType> dimensionType = this.dimensionType.orElse(server.overworld().dimensionTypeRegistration());
		LevelStem dimension = new LevelStem(dimensionType, new VoidChunkGenerator(server));
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
					return new GameMap(name.orElse(null), dimensionHandle.asKey(), metadata.regions())
							.onClose(game -> dimensionHandle.delete());
				}), server);
	}

	private GameResult<RuntimeDimensionHandle> openDimension(MinecraftServer server, RuntimeDimensionConfig config) {
		RuntimeDimensions dimensions = RuntimeDimensions.get(server);
		if (this.dimension.isEmpty()) {
			return GameResult.ok(dimensions.openTemporary(config));
		}

		RuntimeDimensionHandle handle = dimensions.openTemporaryWithKey(this.dimension.get(), config);
		if (handle != null) {
			return GameResult.ok(handle);
		} else {
			return GameResult.error(new TextComponent("Dimension already loaded in '" + this.dimension.get() + "'"));
		}
	}

	private GameResult<Pair<RuntimeDimensionHandle, MapMetadata>> loadMapInto(MinecraftServer server, MapWorldSettings mapWorldSettings, RuntimeDimensionHandle handle) {
		ResourceLocation path = new ResourceLocation(loadFrom.getNamespace(), "maps/" + loadFrom.getPath() + ".zip");

		try (Resource resource = server.getResourceManager().getResource(path)) {
			try (MapExportReader reader = MapExportReader.open(resource.getInputStream())) {
				MapMetadata metadata = reader.loadInto(server, handle.asKey());
				mapWorldSettings.importFrom(metadata.settings());
				return GameResult.ok(Pair.of(handle, metadata));
			}
		} catch (IOException e) {
			LOGGER.error("Failed to load map from {}", path, e);
			return GameResult.fromException("Failed to load map from '" + path + "'", e);
		}
	}
}
