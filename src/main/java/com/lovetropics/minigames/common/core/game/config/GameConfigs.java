package com.lovetropics.minigames.common.core.game.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lovetropics.minigames.Constants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class GameConfigs {
	private static final Logger LOGGER = LogManager.getLogger(GameConfigs.class);

	public static final Map<ResourceLocation, GameConfig> GAME_CONFIGS = new Object2ObjectOpenHashMap<>();

	private static final JsonParser PARSER = new JsonParser();

	@SubscribeEvent
	public static void addReloadListener(AddReloadListenerEvent event) {
		event.addListener((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				GAME_CONFIGS.clear();

				BehaviorReferenceReader behaviorReader = new BehaviorReferenceReader(resourceManager);

				Collection<ResourceLocation> paths = resourceManager.getAllResourceLocations("games", file -> file.endsWith(".json"));
				for (ResourceLocation path : paths) {
					try (IResource resource = resourceManager.getResource(path)) {
						DataResult<GameConfig> result = loadConfig(behaviorReader, path, resource);
						result.result().ifPresent(config -> GAME_CONFIGS.put(config.id, config));

						result.error().ifPresent(error -> {
							LOGGER.error("Failed to load game config at {}: {}", path, error);
						});
					} catch (Exception e) {
						LOGGER.error("Failed to load game config at {}", path, e);
					}
				}
			}, backgroundExecutor);

			return future.thenCompose(stage::markCompleteAwaitingOthers);
		});
	}

	private static DataResult<GameConfig> loadConfig(BehaviorReferenceReader reader, ResourceLocation path, IResource resource) throws IOException {
		try (InputStream input = resource.getInputStream()) {
			JsonElement json = PARSER.parse(new BufferedReader(new InputStreamReader(input)));
			Codec<GameConfig> codec = GameConfig.codec(reader, getIdFromPath(path));
			return codec.parse(JsonOps.INSTANCE, json);
		}
	}

	private static ResourceLocation getIdFromPath(ResourceLocation location) {
		String path = location.getPath();
		String name = path.substring("games/".length(), path.length() - ".json".length());
		return new ResourceLocation(location.getNamespace(), name);
	}
}
