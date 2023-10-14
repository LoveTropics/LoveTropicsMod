package com.lovetropics.minigames.common.core.game.config;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lovetropics.lib.codec.CodecRegistry;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
				CompletableFuture.supplyAsync(() -> listBehaviors(resourceManager), backgroundExecutor)
						.thenAccept(behaviors -> {
							CUSTOM_BEHAVIORS.clear();
							behaviors.forEach(CUSTOM_BEHAVIORS::register);
						})
						.thenApplyAsync(unused -> listConfigs(registryAccess, resourceManager), backgroundExecutor)
						.thenCompose(stage::wait)
						.thenAcceptAsync(configs -> {
							REGISTRY.clear();
							for (GameConfig config : configs) {
								REGISTRY.register(config.id, config);
							}
						}, gameExecutor)
		);
	}

	private static List<GameConfig> listConfigs(RegistryAccess registryAccess, ResourceManager resourceManager) {
		List<GameConfig> configs = new ArrayList<>();

		DynamicOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);

		GAME_LISTER.listMatchingResources(resourceManager).forEach((path, resource) -> {
			try {
				loadConfig(ops, path, resource)
						.resultOrPartial(error -> LOGGER.error("Failed to load game config at {}: {}", path, error))
						.ifPresent(configs::add);
			} catch (Exception e) {
				LOGGER.error("Failed to load game config at {}", path, e);
			}
		});

		return configs;
	}

	private static DataResult<GameConfig> loadConfig(DynamicOps<JsonElement> ops, ResourceLocation path, Resource resource) throws IOException {
		try (BufferedReader reader = resource.openAsReader()) {
			JsonElement json = JsonParser.parseReader(reader);
			Codec<GameConfig> codec = GameConfig.codec(GAME_LISTER.fileToId(path));
			return codec.parse(ops, json);
		}
	}

	private static Map<ResourceLocation, GameBehaviorType<?>> listBehaviors(ResourceManager resourceManager) {
		Map<ResourceLocation, GameBehaviorType<?>> behaviors = new HashMap<>();
		BEHAVIOR_LISTER.listMatchingResources(resourceManager).forEach((path, resource) -> {
			try {
				try (BufferedReader reader = resource.openAsReader()) {
					Dynamic<JsonElement> template = new Dynamic<>(JsonOps.INSTANCE, JsonParser.parseReader(reader));
					behaviors.put(BEHAVIOR_LISTER.fileToId(path), new GameBehaviorType<>(createCustomBehaviorCodec(template)));
				}
			} catch (Exception e) {
				LOGGER.error("Failed to load custom behavior at {}", path, e);
			}
		});
		return behaviors;
	}

	private static Codec<CustomBehavior> createCustomBehaviorCodec(final Dynamic<?> template) {
		return new Codec<>() {
			@Override
			public <T> DataResult<T> encode(CustomBehavior input, DynamicOps<T> ops, T prefix) {
				return DataResult.error(() -> "Encoding unsupported");
			}

			@Override
			public <T> DataResult<Pair<CustomBehavior, T>> decode(DynamicOps<T> ops, T input) {
				BehaviorParameters parameters = new BehaviorParameters(new Dynamic<>(ops, input));
				Dynamic<?> data = parameters.substitute(template);
				return IGameBehavior.LIST_CODEC.parse(data).map(behaviors -> Pair.of(new CustomBehavior(behaviors), input));
			}
		};
	}

	private record CustomBehavior(List<IGameBehavior> behaviors) implements IGameBehavior {
		@Override
		public void register(IGamePhase game, EventRegistrar events) throws GameException {
			for (IGameBehavior behavior : behaviors) {
				behavior.register(game, events);
			}
		}

		@Override
		public void registerState(IGamePhase game, GameStateMap phaseState, GameStateMap instanceState) {
			for (IGameBehavior behavior : behaviors) {
				behavior.registerState(game, phaseState, instanceState);
			}
		}
	}
}
