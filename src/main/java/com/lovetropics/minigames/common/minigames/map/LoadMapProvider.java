package com.lovetropics.minigames.common.minigames.map;

import com.lovetropics.minigames.common.map.MapExportReader;
import com.lovetropics.minigames.common.map.MapMetadata;
import com.lovetropics.minigames.common.map.MapWorldInfo;
import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameMap;
import com.lovetropics.minigames.common.minigames.MinigameResult;
import com.mojang.datafixers.Dynamic;
import net.minecraft.resources.IResource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.DimensionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class LoadMapProvider implements IMinigameMapProvider {
	private static final Logger LOGGER = LogManager.getLogger(LoadMapProvider.class);

	private final ResourceLocation loadFrom;
	private final DimensionType dimension;

	public LoadMapProvider(final ResourceLocation loadFrom, final DimensionType dimension) {
		this.loadFrom = loadFrom;
		this.dimension = dimension;
	}

	public static <T> LoadMapProvider parse(Dynamic<T> root) {
		ResourceLocation loadFrom = new ResourceLocation(root.get("load_from").asString(""));
		DimensionType dimension = DimensionType.byName(new ResourceLocation(root.get("dimension").asString("")));
		return new LoadMapProvider(loadFrom, dimension);
	}

	@Override
	public MinigameResult<Unit> canOpen(IMinigameDefinition definition, MinecraftServer server) {
		ServerWorld world = DimensionManager.getWorld(server, dimension, false, false);

		if (world != null) {
			final ITextComponent minigameName = definition.getName();
			if (world.getPlayers().isEmpty()) {
				DimensionManager.unloadWorld(world);
				return MinigameResult.error(new StringTextComponent("The ").appendSibling(minigameName).appendText(" dimension was not unloaded. Begun unloading, please try again in a few seconds.").applyTextStyle(TextFormatting.RED));
			}

			return MinigameResult.error(new StringTextComponent("Cannot start minigame as players are in ").appendSibling(minigameName).appendText(" dimension. Make them teleport out first.").applyTextStyle(TextFormatting.RED));
		}

		return MinigameResult.ok();
	}

	@Override
	public CompletableFuture<MinigameMap> open(MinecraftServer server) {
		return loadMap(server)
				.thenApplyAsync(metadata -> {
					MinigameMap map = new MinigameMap(dimension, metadata.regions);

					ServerWorld world = server.getWorld(dimension);
					ServerWorld overworld = server.getWorld(DimensionType.OVERWORLD);
					world.worldInfo = new MapWorldInfo(overworld.getWorldInfo(), metadata.settings);

					return map;
				}, server);
	}

	private CompletableFuture<MapMetadata> loadMap(MinecraftServer server) {
		return CompletableFuture.supplyAsync(() -> {
			ResourceLocation path = new ResourceLocation(loadFrom.getNamespace(), "maps/" + loadFrom.getPath() + ".zip");

			try (IResource resource = server.getResourceManager().getResource(path)) {
				try (MapExportReader reader = MapExportReader.open(resource.getInputStream())) {
					return reader.loadInto(server, dimension);
				}
			} catch (IOException e) {
				LOGGER.error("Failed to load map from {}", path, e);
				throw new CompletionException(e);
			}
		}, Util.getServerExecutor());
	}

	@Override
	public void close(IMinigameInstance minigame) {
		ServerWorld world = minigame.getWorld();
		DimensionManager.unloadWorld(world);
	}
}
