package com.lovetropics.minigames.common.core.game.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lovetropics.lib.codec.CodecRegistry;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.Util;
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
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

// TODO: Replace with a dynamic registry - currently blocked by not being able to /reload those
@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class GameConfigs {
	private static final Logger LOGGER = LogManager.getLogger(GameConfigs.class);

	public static final CodecRegistry<ResourceLocation, GameConfig> REGISTRY = CodecRegistry.resourceLocationKeys();
	public static final CodecRegistry<ResourceLocation, GameBehaviorType<?>> CUSTOM_BEHAVIORS = CodecRegistry.resourceLocationKeys();

	private static final FileToIdConverter GAME_LISTER = FileToIdConverter.json("games");
	private static final FileToIdConverter BEHAVIOR_LISTER = FileToIdConverter.json("behaviors");

	@SubscribeEvent
	public static void addReloadListener(AddReloadListenerEvent event) {
		RegistryAccess registryAccess = event.getRegistryAccess();
		event.addListener((stage, resourceManager, preparationsProfiler, reloadProfiler, backgroundExecutor, gameExecutor) ->
				load(resourceManager, backgroundExecutor, gameExecutor, registryAccess)
						.thenCompose(stage::wait)
						.thenAcceptAsync(configs -> {
							REGISTRY.clear();
							for (GameConfig config : configs) {
								REGISTRY.register(config.id, config);
							}
						}, gameExecutor)
		);
	}

	private static CompletableFuture<List<GameConfig>> load(ResourceManager resourceManager, Executor backgroundExecutor, Executor gameExecutor, RegistryAccess registryAccess) {
		return CompletableFuture.supplyAsync(() -> listBehaviors(resourceManager, backgroundExecutor), backgroundExecutor)
				.thenCompose(f -> f)
				.thenAccept(behaviors -> {
					CUSTOM_BEHAVIORS.clear();
					behaviors.forEach(CUSTOM_BEHAVIORS::register);
				})
				.thenComposeAsync(unused -> listConfigs(registryAccess, resourceManager, backgroundExecutor), backgroundExecutor);
	}

	private static CompletableFuture<List<GameConfig>> listConfigs(RegistryAccess registryAccess, ResourceManager resourceManager, Executor executor) {
		DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
		List<CompletableFuture<GameConfig>> futures = GAME_LISTER.listMatchingResources(resourceManager).entrySet().stream()
				.map(entry -> CompletableFuture.supplyAsync(() -> tryLoadConfig(ops, entry.getKey(), entry.getValue()), executor))
				.toList();
		return Util.sequence(futures).thenApply(configs -> configs.stream().filter(Objects::nonNull).toList());
	}

	@Nullable
	private static GameConfig tryLoadConfig(DynamicOps<JsonElement> ops, ResourceLocation path, Resource resource) {
		try {
			return loadConfig(ops, path, resource)
					.resultOrPartial(error -> LOGGER.error("Failed to load game config at {}: {}", path, error))
					.orElse(null);
		} catch (Exception e) {
			LOGGER.error("Failed to load game config at {}", path, e);
			return null;
		}
	}

	private static DataResult<GameConfig> loadConfig(DynamicOps<JsonElement> ops, ResourceLocation path, Resource resource) throws IOException {
		try (BufferedReader reader = resource.openAsReader()) {
			JsonElement json = JsonParser.parseReader(reader);
			Codec<GameConfig> codec = GameConfig.codec(GAME_LISTER.fileToId(path));
			return codec.parse(ops, json);
		}
	}

	private static CompletableFuture<Map<ResourceLocation, GameBehaviorType<?>>> listBehaviors(ResourceManager resourceManager, Executor executor) {
		List<CompletableFuture<Map.Entry<ResourceLocation, GameBehaviorType<?>>>> futures = BEHAVIOR_LISTER.listMatchingResources(resourceManager).entrySet().stream()
				.map(entry -> CompletableFuture.supplyAsync(() -> tryLoadBehavior(entry.getValue(), entry.getKey()), executor))
				.toList();
		return Util.sequence(futures).thenApply(behaviors -> behaviors.stream()
				.filter(Objects::nonNull)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
		);
	}

	@Nullable
	private static Map.Entry<ResourceLocation, GameBehaviorType<?>> tryLoadBehavior(Resource resource, ResourceLocation path) {
		try {
			try (BufferedReader reader = resource.openAsReader()) {
				Dynamic<JsonElement> template = new Dynamic<>(JsonOps.INSTANCE, JsonParser.parseReader(reader));
				ResourceLocation id = BEHAVIOR_LISTER.fileToId(path);
				return Map.entry(id, new GameBehaviorType<>(createCustomBehaviorCodec(template)));
			}
		} catch (Exception e) {
			LOGGER.error("Failed to load custom behavior at {}", path, e);
			return null;
		}
	}

	private static Codec<IGameBehavior> createCustomBehaviorCodec(final Dynamic<?> template) {
		return new Codec<>() {
			@Override
			public <T> DataResult<T> encode(IGameBehavior input, DynamicOps<T> ops, T prefix) {
				return DataResult.error(() -> "Encoding unsupported");
			}

			@Override
			public <T> DataResult<Pair<IGameBehavior, T>> decode(DynamicOps<T> ops, T input) {
				BehaviorParameters parameters = new BehaviorParameters(new Dynamic<>(ops, input));
				return IGameBehavior.CODEC.decode(parameters.substitute(template))
						.map(pair -> pair.mapSecond(o -> input));
			}
		};
	}
}
