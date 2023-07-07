package com.lovetropics.minigames.common.core.game.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lovetropics.lib.codec.CodecRegistry;
import com.lovetropics.minigames.Constants;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// TODO: Replace with a dynamic registry - currently blocked by not being able to /reload those
@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class GameConfigs {
	private static final Logger LOGGER = LogManager.getLogger(GameConfigs.class);

	public static final CodecRegistry<ResourceLocation, GameConfig> REGISTRY = CodecRegistry.resourceLocationKeys();

	private static final FileToIdConverter FILE_TO_ID_CONVERTER = FileToIdConverter.json("games");

	@SubscribeEvent
	public static void addReloadListener(AddReloadListenerEvent event) {
		RegistryAccess registryAccess = event.getRegistryAccess();
		event.addListener((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) ->
				CompletableFuture.supplyAsync(() -> listConfigs(registryAccess, resourceManager), backgroundExecutor)
						.thenCompose(stage::wait)
						.thenAcceptAsync(configs -> {
							REGISTRY.clear();
							for (GameConfig config : configs) {
								REGISTRY.register(config.id, config);
							}
						}, gameExecutor));
	}

	private static List<GameConfig> listConfigs(RegistryAccess registryAccess, ResourceManager resourceManager) {
		List<GameConfig> configs = new ArrayList<>();

		DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
		BehaviorReferenceReader behaviorReader = new BehaviorReferenceReader(resourceManager);

		FILE_TO_ID_CONVERTER.listMatchingResources(resourceManager).forEach((path, resource) -> {
			try {
				loadConfig(ops, behaviorReader, path, resource)
						.resultOrPartial(error -> LOGGER.error("Failed to load game config at {}: {}", path, error))
						.ifPresent(configs::add);
			} catch (Exception e) {
				LOGGER.error("Failed to load game config at {}", path, e);
			}
		});

		return configs;
	}

	private static DataResult<GameConfig> loadConfig(DynamicOps<JsonElement> ops, BehaviorReferenceReader behaviorReader, ResourceLocation path, Resource resource) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.open(), StandardCharsets.UTF_8))) {
			JsonElement json = JsonParser.parseReader(reader);
			Codec<GameConfig> codec = GameConfig.codec(behaviorReader, FILE_TO_ID_CONVERTER.fileToId(path));
			return codec.parse(ops, json);
		}
	}
}
