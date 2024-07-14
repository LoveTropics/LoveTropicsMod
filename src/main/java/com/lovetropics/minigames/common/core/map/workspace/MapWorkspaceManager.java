package com.lovetropics.minigames.common.core.map.workspace;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensionHandle;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensions;
import com.lovetropics.minigames.common.core.map.MapWorldInfo;
import com.lovetropics.minigames.common.core.map.MapWorldSettings;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class MapWorkspaceManager extends SavedData {
	private static final String ID = LoveTropics.ID + "_map_workspace_manager";

	private final MinecraftServer server;
	private final Map<String, MapWorkspace> workspaces = new Object2ObjectOpenHashMap<>();

	private MapWorkspaceManager(MinecraftServer server) {
		this.server = server;
	}

	public static MapWorkspaceManager get(MinecraftServer server) {
		ServerLevel overworld = server.overworld();
		return overworld.getDataStorage().computeIfAbsent(new Factory<>(
				() -> new MapWorkspaceManager(server),
				(tag, registries) -> MapWorkspaceManager.load(server, tag)
		), ID);
	}

	public CompletableFuture<MapWorkspace> openWorkspace(String id, WorkspaceDimensionConfig dimensionConfig) {
		MapWorldSettings settings = MapWorldSettings.createFromOverworld(server);

		return CompletableFuture.supplyAsync(() -> getOrCreateDimension(id, dimensionConfig, settings), server)
				.thenApplyAsync(dimensionHandle -> {
					MapWorkspace workspace = new MapWorkspace(id, dimensionConfig, settings, dimensionHandle);
					this.workspaces.putIfAbsent(id, workspace);

					return workspace;
				}, this.server);
	}

	private RuntimeDimensionHandle getOrCreateDimension(String id, WorkspaceDimensionConfig dimensionConfig, MapWorldSettings mapSettings) {
		return RuntimeDimensions.get(server).getOrOpenPersistent(LoveTropics.location(id), () -> {
			MapWorldInfo worldInfo = MapWorldInfo.create(server, mapSettings);
			return dimensionConfig.toRuntimeConfig(worldInfo);
		});
	}

	public boolean deleteWorkspace(String id) {
		MapWorkspace workspace = this.workspaces.remove(id);
		if (workspace != null) {
			workspace.dimensionHandle().delete();
			return true;
		}
		return false;
	}

	@Nullable
	public MapWorkspace getWorkspace(String id) {
		return this.workspaces.get(id);
	}

	@Nullable
	public MapWorkspace getWorkspace(ResourceKey<Level> dimension) {
		ResourceLocation name = dimension.location();
		if (!name.getNamespace().equals(LoveTropics.ID)) {
			return null;
		}
		return this.workspaces.get(name.getPath());
	}

	public Set<String> getWorkspaceIds() {
		return workspaces.keySet();
	}

	public boolean hasWorkspace(String id) {
		return this.workspaces.containsKey(id);
	}

	public boolean isWorkspace(ResourceKey<Level> dimension) {
		return getWorkspace(dimension) != null;
	}

	@Override
	public CompoundTag save(CompoundTag root, HolderLookup.Provider registries) {
		ListTag workspaceList = new ListTag();

		RegistryOps<Tag> ops = registries.createSerializationContext(NbtOps.INSTANCE);

		for (Map.Entry<String, MapWorkspace> entry : this.workspaces.entrySet()) {
			MapWorkspaceData data = entry.getValue().intoData();
			MapWorkspaceData.CODEC.encodeStart(ops, data)
					.result().ifPresent(workspaceList::add);
		}

		root.put("workspaces", workspaceList);

		return root;
	}

	private static MapWorkspaceManager load(MinecraftServer server, CompoundTag root) {
		MapWorkspaceManager manager = new MapWorkspaceManager(server);

		manager.workspaces.clear();

		ListTag workspaceList = root.getList("workspaces", Tag.TAG_COMPOUND);

		DynamicOps<Tag> ops = server.registryAccess().createSerializationContext(NbtOps.INSTANCE);

		for (int i = 0; i < workspaceList.size(); i++) {
			CompoundTag workspaceRoot = workspaceList.getCompound(i);

			DataResult<MapWorkspaceData> result = MapWorkspaceData.CODEC.parse(ops, workspaceRoot);
			result.result().ifPresent(workspaceData -> {
				RuntimeDimensionHandle dimensionHandle = manager.getOrCreateDimension(workspaceData.id(), workspaceData.dimension(), workspaceData.worldSettings());
				MapWorkspace workspace = workspaceData.create(dimensionHandle);
				manager.workspaces.put(workspaceData.id(), workspace);
			});

			result.error().ifPresent(error -> {
				LoveTropics.LOGGER.warn("Failed to load map workspace: {}", error);
			});
		}

		return manager;
	}

	@Override
	public boolean isDirty() {
		return true;
	}
}
