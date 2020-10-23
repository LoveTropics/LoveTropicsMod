package com.lovetropics.minigames.common.minigames.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lovetropics.minigames.common.minigames.IMinigameManager;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import net.minecraft.resources.IResource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class MinigameConfigs {
	private static final Logger LOGGER = LogManager.getLogger(MinigameConfigs.class);

	private static final Set<MinigameConfig> GAME_CONFIGS = new HashSet<>();

	private static final JsonParser PARSER = new JsonParser();

	public static void init(MinecraftServer server) {
		GAME_CONFIGS.clear();

		server.getResourceManager().addReloadListener((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				IMinigameManager manager = MinigameManager.getInstance();
				for (MinigameConfig config : GAME_CONFIGS) {
					manager.unregister(config.id);
				}

				GAME_CONFIGS.clear();

				Collection<ResourceLocation> paths = resourceManager.getAllResourceLocations("games", file -> file.endsWith(".json"));
				for (ResourceLocation path : paths) {
					try (IResource resource = resourceManager.getResource(path)) {
						MinigameConfig config = loadConfig(path, resource);
						GAME_CONFIGS.add(config);
						manager.register(config);
					} catch (IOException e) {
						LOGGER.error("Failed to load game config at {}", path, e);
					}
				}
			}, backgroundExecutor);

			return future.thenCompose(stage::markCompleteAwaitingOthers);
		});
	}

	private static MinigameConfig loadConfig(ResourceLocation path, IResource resource) throws IOException {
		try (InputStream input = resource.getInputStream()) {
			JsonElement json = PARSER.parse(new BufferedReader(new InputStreamReader(input)));
			Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, json);
			return MinigameConfig.deserialize(getIdFromPath(path), dynamic);
		}
	}

	private static ResourceLocation getIdFromPath(ResourceLocation location) {
		String path = location.getPath();
		String name = path.substring("games/".length(), path.length() - ".json".length());
		return new ResourceLocation(location.getNamespace(), name);
	}
}
