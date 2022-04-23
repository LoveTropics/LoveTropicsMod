package com.lovetropics.minigames.common.core.game.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lovetropics.lib.codec.CodecRegistry;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.util.DynamicRegistryReadingOps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class GameConfigs {
	private static final Logger LOGGER = LogManager.getLogger(GameConfigs.class);

	public static final CodecRegistry<ResourceLocation, GameConfig> REGISTRY = CodecRegistry.resourceLocationKeys();

	private static final JsonParser PARSER = new JsonParser();

	@SubscribeEvent
	public static void addReloadListener(AddReloadListenerEvent event) {
		event.addListener((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				REGISTRY.clear();

				DynamicOps<JsonElement> ops = DynamicRegistryReadingOps.create(resourceManager, JsonOps.INSTANCE);

				BehaviorReferenceReader behaviorReader = new BehaviorReferenceReader(resourceManager);

				Collection<ResourceLocation> paths = resourceManager.listResources("games", file -> file.endsWith(".json"));
				for (ResourceLocation path : paths) {
					try (IResource resource = resourceManager.getResource(path)) {
						DataResult<GameConfig> result = loadConfig(ops, behaviorReader, path, resource);
						result.result().ifPresent(config -> REGISTRY.register(config.id, config));

						result.error().ifPresent(error -> {
							LOGGER.error("Failed to load game config at {}: {}", path, error);
						});
					} catch (Exception e) {
						LOGGER.error("Failed to load game config at {}", path, e);
					}
				}
			}, backgroundExecutor);

			return future.thenCompose(stage::wait);
		});
	}

	private static DataResult<GameConfig> loadConfig(
			DynamicOps<JsonElement> ops, BehaviorReferenceReader reader,
			ResourceLocation path, IResource resource
	) throws IOException {
		try (InputStream input = resource.getInputStream()) {
			JsonElement json = PARSER.parse(new BufferedReader(new InputStreamReader(input)));
			Codec<GameConfig> codec = GameConfig.codec(reader, getIdFromPath(path));
			return codec.parse(ops, json);
		}
	}

	private static ResourceLocation getIdFromPath(ResourceLocation location) {
		String path = location.getPath();
		String name = path.substring("games/".length(), path.length() - ".json".length());
		return new ResourceLocation(location.getNamespace(), name);
	}
}
