package com.lovetropics.minigames.common.core.dimension;

import com.google.common.base.Preconditions;
import com.lovetropics.minigames.Constants;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
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
import java.util.Collection;
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
			instance.stop();
			RuntimeDimensions.instance = null;
		}
	}

	public static RuntimeDimensions get(MinecraftServer server) {
		RuntimeDimensions instance = RuntimeDimensions.instance;
		Preconditions.checkState(instance != null && instance.server == server, "runtime dimensions not yet initialized");
		return instance;
	}

	public RuntimeDimensionHandle getOrOpenPersistent(ResourceLocation key, Supplier<RuntimeDimensionConfig> config) {
		ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, key);
		ServerLevel world = this.server.getLevel(worldKey);
		if (world != null) {
			this.deletionQueue.remove(world);
			return new RuntimeDimensionHandle(this, world);
		}

		return this.openLevel(key, config.get(), false);
	}

	public RuntimeDimensionHandle openTemporary(RuntimeDimensionConfig config) {
		ResourceLocation key = generateTemporaryDimensionKey();
		return this.openLevel(key, config, true);
	}

	@Nullable
	public RuntimeDimensionHandle openTemporaryWithKey(ResourceLocation key, RuntimeDimensionConfig config) {
		ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, key);
		if (this.server.getLevel(worldKey) == null) {
			return this.openLevel(key, config, true);
		} else {
			return null;
		}
	}

	private RuntimeDimensionHandle openLevel(ResourceLocation key, RuntimeDimensionConfig config, boolean temporary) {
		ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, key);

		MappedRegistry<LevelStem> dimensionsRegistry = getLevelStemRegistry(this.server);
		dimensionsRegistry.unfreeze();
		dimensionsRegistry.register(ResourceKey.create(Registries.LEVEL_STEM, key), config.dimension(), Lifecycle.stable());
		dimensionsRegistry.freeze();

		ServerLevel level = new ServerLevel(
				this.server, Util.backgroundExecutor(), this.server.storageSource,
				config.worldInfo(),
				levelKey,
				config.dimension(),
				VoidChunkStatusListener.INSTANCE,
				false,
				BiomeManager.obfuscateSeed(config.seed()),
				List.of(),
				false,
				server.overworld().getRandomSequences()
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

		this.server.levels.put(levelKey, level);
		this.server.markWorldsDirty();

		if (temporary) {
			this.temporaryDimensions.add(levelKey);
		}

		MinecraftForge.EVENT_BUS.post(new LevelEvent.Load(level));

		level.tick(() -> true);

		return new RuntimeDimensionHandle(this, level);
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

	private void deleteDimension(ServerLevel level) {
		ResourceKey<Level> dimensionKey = level.dimension();

		if (this.server.levels.remove(dimensionKey, level)) {
			this.server.markWorldsDirty();

			this.temporaryDimensions.remove(dimensionKey);

			MinecraftForge.EVENT_BUS.post(new LevelEvent.Unload(level));

			MappedRegistry<LevelStem> dimensionsRegistry = getLevelStemRegistry(this.server);

			dimensionsRegistry.unfreeze();
			RegistryEntryRemover.remove(dimensionsRegistry, dimensionKey.location());
			dimensionsRegistry.freeze();

			LevelStorageSource.LevelStorageAccess save = this.server.storageSource;
			Path dimensionPath = save.getDimensionPath(dimensionKey);
			Util.ioPool().submit(() -> {
				try {
					level.close();
				} catch (IOException e) {
					LOGGER.error("Failed to close runtime level", e);
				}
				deleteWorldDirectory(dimensionPath);
			});
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

	private static MappedRegistry<LevelStem> getLevelStemRegistry(MinecraftServer server) {
		return (MappedRegistry<LevelStem>) server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
	}

	private static ResourceLocation generateTemporaryDimensionKey() {
		String random = RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789");
		return new ResourceLocation(Constants.MODID, "tmp_" + random);
	}

	public boolean isTemporaryDimension(ResourceKey<Level> dimension) {
		return this.temporaryDimensions.contains(dimension);
	}

	public RuntimeDimensionHandle handleForTemporaryDimension(ResourceKey<Level> dimension) {
		if (!this.isTemporaryDimension(dimension)) {
			throw new IllegalArgumentException("must be a temporary dimension");
		}
		return new RuntimeDimensionHandle(this, this.server.getLevel(dimension));
	}

	public Collection<ResourceKey<Level>> getTemporaryDimensions() {
		return this.temporaryDimensions;
	}
}
