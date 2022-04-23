package com.lovetropics.minigames.common.core.dimension;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.Constants;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class RuntimeDimensions {
	private static final Logger LOGGER = LogManager.getLogger(RuntimeDimensions.class);

	private static RuntimeDimensions instance;

	private final MinecraftServer server;

	private final Set<ServerLevel> deletionQueue = new ReferenceOpenHashSet<>();
	private final Set<ResourceKey<Level>> temporaryDimensions = new ReferenceOpenHashSet<>();

	private RuntimeDimensions(MinecraftServer server) {
		this.server = server;
	}

	@SubscribeEvent
	public static void onServerAboutToStart(ServerAboutToStartEvent event) {
		instance = new RuntimeDimensions(event.getServer());
	}

	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			return;
		}

		RuntimeDimensions instance = RuntimeDimensions.instance;
		if (instance != null) {
			instance.tick();
		}
	}

	@SubscribeEvent
	public static void onServerStopping(ServerStoppingEvent event) {
		RuntimeDimensions.tryStop();
	}

	public static void onServerStoppingUnsafely(MinecraftServer server) {
		RuntimeDimensions.tryStop();
	}

	private static void tryStop() {
		RuntimeDimensions instance = RuntimeDimensions.instance;
		if (instance != null) {
			RuntimeDimensions.instance = null;
			instance.stop();
		}
	}

	public static RuntimeDimensions get(MinecraftServer server) {
		RuntimeDimensions instance = RuntimeDimensions.instance;
		Preconditions.checkState(instance != null && instance.server == server, "runtime dimensions not yet initialized");
		return instance;
	}

	public RuntimeDimensionHandle getOrOpenPersistent(ResourceLocation key, Supplier<RuntimeDimensionConfig> config) {
		ResourceKey<Level> worldKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, key);
		ServerLevel world = this.server.getLevel(worldKey);
		if (world != null) {
			this.deletionQueue.remove(world);
			return new RuntimeDimensionHandle(this, world);
		}

		return this.openWorld(key, config.get(), false);
	}

	public RuntimeDimensionHandle openTemporary(RuntimeDimensionConfig config) {
		ResourceLocation key = generateTemporaryDimensionKey();
		return this.openWorld(key, config, true);
	}

	@Nullable
	public RuntimeDimensionHandle openTemporaryWithKey(ResourceLocation key, RuntimeDimensionConfig config) {
		ResourceKey<Level> worldKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, key);
		if (this.server.getLevel(worldKey) == null) {
			return this.openWorld(key, config, true);
		} else {
			return null;
		}
	}

	RuntimeDimensionHandle openWorld(ResourceLocation key, RuntimeDimensionConfig config, boolean temporary) {
		ResourceKey<Level> worldKey = ResourceKey.create(Registry.DIMENSION_REGISTRY, key);

		MappedRegistry<LevelStem> dimensionsRegistry = getDimensionsRegistry(this.server);
		dimensionsRegistry.register(ResourceKey.create(Registry.LEVEL_STEM_REGISTRY, key), config.dimension, Lifecycle.stable());

		ServerLevel world = new ServerLevel(
				this.server, Util.backgroundExecutor(), this.server.storageSource,
				config.worldInfo,
				worldKey,
				config.dimension.typeHolder(),
				VoidChunkStatusListener.INSTANCE,
				config.dimension.generator(),
				false,
				BiomeManager.obfuscateSeed(config.seed),
				ImmutableList.of(),
				false
		) {
			@Override
			public void save(@Nullable ProgressListener progress, boolean flush, boolean skipSave) {
				if (temporary) {
					if (!flush && temporaryDimensions.contains(dimension())) {
						super.save(progress, false, skipSave);
					}
				} else {
					super.save(progress, flush, skipSave);
				}
			}
		};

		this.server.levels.put(worldKey, world);
		this.server.markWorldsDirty();

		if (temporary) {
			this.temporaryDimensions.add(worldKey);
		}

		MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));

		world.tick(() -> true);

		return new RuntimeDimensionHandle(this, world);
	}

	void tick() {
		if (!this.deletionQueue.isEmpty()) {
			this.deletionQueue.removeIf(this::tickDimensionDeletion);
		}
	}

	boolean tickDimensionDeletion(ServerLevel world) {
		if (this.isWorldUnloaded(world)) {
			this.deleteDimension(world);
			return true;
		} else {
			this.kickPlayersFrom(world);
			return false;
		}
	}

	private void stop() {
		ArrayList<ResourceKey<Level>> temporaryDimensions = new ArrayList<>(this.temporaryDimensions);
		for (ResourceKey<Level> dimension : temporaryDimensions) {
			ServerLevel world = this.server.getLevel(dimension);
			if (world != null) {
				this.kickPlayersFrom(world);
				this.deleteDimension(world);
			}
		}
	}

	void enqueueDeletion(ServerLevel world) {
		CompletableFuture.runAsync(() -> {
			this.deletionQueue.add(world);
		}, this.server);
	}

	private void kickPlayersFrom(ServerLevel world) {
		if (world.players().isEmpty()) {
			return;
		}

		ServerLevel overworld = this.server.overworld();
		BlockPos spawnPos = overworld.getSharedSpawnPos();
		float spawnAngle = overworld.getSharedSpawnAngle();

		List<ServerPlayer> players = new ArrayList<>(world.players());
		for (ServerPlayer player : players) {
			player.teleportTo(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, spawnAngle, 0.0F);
		}
	}

	private boolean isWorldUnloaded(ServerLevel world) {
		return world.players().isEmpty() && world.getChunkSource().getLoadedChunksCount() <= 0;
	}

	private void deleteDimension(ServerLevel world) {
		ResourceKey<Level> dimensionKey = world.dimension();

		if (this.server.levels.remove(dimensionKey, world)) {
			this.server.markWorldsDirty();

			this.temporaryDimensions.remove(dimensionKey);

			MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(world));

			MappedRegistry<LevelStem> dimensionsRegistry = getDimensionsRegistry(this.server);

			dimensionsRegistry.unfreeze();
			RegistryEntryRemover.remove(dimensionsRegistry, dimensionKey.location());
			dimensionsRegistry.freeze();

			LevelStorageSource.LevelStorageAccess save = this.server.storageSource;
			deleteWorldDirectory(save.getDimensionPath(dimensionKey));
		}
	}

	private static void deleteWorldDirectory(Path worldDirectory) {
		if (Files.exists(worldDirectory)) {
			try {
				FileUtils.deleteDirectory(worldDirectory.toFile());
			} catch (IOException e) {
				LOGGER.warn("Failed to delete world directory", e);
				try {
					FileUtils.forceDeleteOnExit(worldDirectory.toFile());
				} catch (IOException ignored) {
				}
			}
		}
	}

	private static MappedRegistry<LevelStem> getDimensionsRegistry(MinecraftServer server) {
		WorldGenSettings generatorSettings = server.getWorldData().worldGenSettings();
		return (MappedRegistry<LevelStem>) generatorSettings.dimensions();
	}

	private static ResourceLocation generateTemporaryDimensionKey() {
		String random = RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789");
		return new ResourceLocation(Constants.MODID, "tmp_" + random);
	}

	public boolean isTemporaryDimension(ResourceKey<Level> dimension) {
		return this.temporaryDimensions.contains(dimension);
	}
}
