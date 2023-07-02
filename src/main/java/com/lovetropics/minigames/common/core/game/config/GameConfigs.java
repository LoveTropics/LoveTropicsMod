package com.lovetropics.minigames.common.core.game.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lovetropics.lib.codec.CodecRegistry;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.mixin.TagManagerAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

// TODO: Replace with a dynamic registry - currently blocked by not being able to /reload those
@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class GameConfigs {
	private static final Logger LOGGER = LogManager.getLogger(GameConfigs.class);

	public static final CodecRegistry<ResourceLocation, GameConfig> REGISTRY = CodecRegistry.resourceLocationKeys();

	private static final FileToIdConverter FILE_TO_ID_CONVERTER = FileToIdConverter.json("games");

	@SubscribeEvent
	public static void addReloadListener(AddReloadListenerEvent event) {
		event.addListener((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) -> {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				REGISTRY.clear();

				RegistryAccess registryAccess = getRegistryAccess(event.getServerResources());
				DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

				BehaviorReferenceReader behaviorReader = new BehaviorReferenceReader(resourceManager);

				FILE_TO_ID_CONVERTER.listMatchingResources(resourceManager).forEach((path, resource) -> {
					try {
						DataResult<GameConfig> result = loadConfig(ops, behaviorReader, path, resource);
						result.result().ifPresent(config -> REGISTRY.register(config.id, config));

						result.error().ifPresent(error -> {
							LOGGER.error("Failed to load game config at {}: {}", path, error);
						});
					} catch (Exception e) {
						LOGGER.error("Failed to load game config at {}", path, e);
					}
				});
			}, backgroundExecutor);

			return future.thenCompose(stage::wait);
		});
	}

	// TODO: This is a terrible hack. PR to Forge to expose this through the event?
	private static RegistryAccess getRegistryAccess(ReloadableServerResources serverResources) {
		for (PreparableReloadListener listener : serverResources.listeners()) {
			if (listener instanceof TagManagerAccessor tagManager) {
				return tagManager.ltminigames$getRegistryAccess();
			}
		}
		throw new IllegalStateException("Could not unpack RegistryAcess from ReloadableServerResources");
	}

	private static DataResult<GameConfig> loadConfig(
			DynamicOps<JsonElement> ops, BehaviorReferenceReader reader,
			ResourceLocation path, Resource resource
	) throws IOException {
		try (InputStream input = resource.open()) {
			JsonElement json = JsonParser.parseReader(new BufferedReader(new InputStreamReader(input)));
			Codec<GameConfig> codec = GameConfig.codec(reader, FILE_TO_ID_CONVERTER.fileToId(path));
			return codec.parse(ops, json);
		}
	}
}
