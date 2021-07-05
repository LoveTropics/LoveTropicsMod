package com.lovetropics.minigames.common.core.dimension;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.Constants;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.SaveFormat;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
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

	private final Set<ServerWorld> deletionQueue = new ReferenceOpenHashSet<>();
	private final Set<RegistryKey<World>> temporaryDimensions = new ReferenceOpenHashSet<>();

	private RuntimeDimensions(MinecraftServer server) {
		this.server = server;
	}

	@SubscribeEvent
	public static void onServerAboutToStart(FMLServerAboutToStartEvent event) {
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
	public static void onServerStopping(FMLServerStoppingEvent event) {
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
		RegistryKey<World> worldKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, key);
		ServerWorld world = this.server.getWorld(worldKey);
		if (world != null) {
			this.deletionQueue.remove(world);
			return new RuntimeDimensionHandle(this, world);
		}

		return this.openWorld(key, config.get());
	}

	public RuntimeDimensionHandle openTemporary(RuntimeDimensionConfig config) {
		ResourceLocation key = generateTemporaryDimensionKey();
		return this.openWorld(key, config);
	}

	RuntimeDimensionHandle openWorld(ResourceLocation key, RuntimeDimensionConfig config) {
		RegistryKey<World> worldKey = RegistryKey.getOrCreateKey(Registry.WORLD_KEY, key);

		SimpleRegistry<Dimension> dimensionsRegistry = getDimensionsRegistry(this.server);
		dimensionsRegistry.register(RegistryKey.getOrCreateKey(Registry.DIMENSION_KEY, key), config.dimension, Lifecycle.stable());

		ServerWorld world = new ServerWorld(
				this.server, Util.getServerExecutor(), this.server.anvilConverterForAnvilFile,
				config.worldInfo,
				worldKey,
				config.dimension.getDimensionType(),
				VoidChunkStatusListener.INSTANCE,
				config.dimension.getChunkGenerator(),
				false,
				BiomeManager.getHashedSeed(config.seed),
				ImmutableList.of(),
				false
		);

		this.server.worlds.put(worldKey, world);
		this.server.markWorldsDirty();

		MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));

		world.tick(() -> true);

		return new RuntimeDimensionHandle(this, world);
	}

	void tick() {
		if (!this.deletionQueue.isEmpty()) {
			this.deletionQueue.removeIf(this::tickDimensionDeletion);
		}
	}

	boolean tickDimensionDeletion(ServerWorld world) {
		if (this.isWorldUnloaded(world)) {
			this.deleteDimension(world);
			return true;
		} else {
			this.kickPlayersFrom(world);
			return false;
		}
	}

	private void stop() {
		ArrayList<RegistryKey<World>> temporaryDimensions = new ArrayList<>(this.temporaryDimensions);
		for (RegistryKey<World> dimension : temporaryDimensions) {
			ServerWorld world = this.server.getWorld(dimension);
			if (world != null) {
				this.kickPlayersFrom(world);
				this.deleteDimension(world);
			}
		}
	}

	void enqueueDeletion(ServerWorld world) {
		CompletableFuture.runAsync(() -> {
			this.deletionQueue.add(world);
		}, this.server);
	}

	private void kickPlayersFrom(ServerWorld world) {
		if (world.getPlayers().isEmpty()) {
			return;
		}

		ServerWorld overworld = this.server.func_241755_D_();
		BlockPos spawnPos = overworld.getSpawnPoint();
		float spawnAngle = overworld.getSpawnAngle();

		List<ServerPlayerEntity> players = new ArrayList<>(world.getPlayers());
		for (ServerPlayerEntity player : players) {
			player.teleport(overworld, spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, spawnAngle, 0.0F);
		}
	}

	private boolean isWorldUnloaded(ServerWorld world) {
		return world.getPlayers().isEmpty() && world.getChunkProvider().getLoadedChunkCount() <= 0;
	}

	private void deleteDimension(ServerWorld world) {
		RegistryKey<World> dimensionKey = world.getDimensionKey();

		if (this.server.worlds.remove(dimensionKey, world)) {
			this.server.markWorldsDirty();

			this.temporaryDimensions.remove(dimensionKey);

			MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(world));

			SimpleRegistry<Dimension> dimensionsRegistry = getDimensionsRegistry(this.server);
			RegistryEntryRemover.remove(dimensionsRegistry, dimensionKey.getLocation());

			SaveFormat.LevelSave save = this.server.anvilConverterForAnvilFile;
			deleteWorldDirectory(save.getDimensionFolder(dimensionKey));
		}
	}

	private static void deleteWorldDirectory(File worldDirectory) {
		if (worldDirectory.exists()) {
			try {
				FileUtils.deleteDirectory(worldDirectory);
			} catch (IOException e) {
				LOGGER.warn("Failed to delete world directory", e);
				try {
					FileUtils.forceDeleteOnExit(worldDirectory);
				} catch (IOException ignored) {
				}
			}
		}
	}

	private static SimpleRegistry<Dimension> getDimensionsRegistry(MinecraftServer server) {
		DimensionGeneratorSettings generatorSettings = server.getServerConfiguration().getDimensionGeneratorSettings();
		return generatorSettings.func_236224_e_();
	}

	private static ResourceLocation generateTemporaryDimensionKey() {
		String random = RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789");
		return new ResourceLocation(Constants.MODID, "tmp_" + random);
	}
}
