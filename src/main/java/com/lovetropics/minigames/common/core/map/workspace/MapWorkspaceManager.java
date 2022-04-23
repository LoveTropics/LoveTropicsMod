package com.lovetropics.minigames.common.core.map.workspace;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensionHandle;
import com.lovetropics.minigames.common.core.dimension.RuntimeDimensions;
import com.lovetropics.minigames.common.core.map.MapWorldInfo;
import com.lovetropics.minigames.common.core.map.MapWorldSettings;
import com.lovetropics.minigames.common.util.DynamicRegistryReadingOps;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenSettingsExport;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class MapWorkspaceManager extends WorldSavedData {
	private static final String ID = Constants.MODID + "_map_workspace_manager";

	private final MinecraftServer server;
	private final Map<String, MapWorkspace> workspaces = new Object2ObjectOpenHashMap<>();

	private MapWorkspaceManager(MinecraftServer server) {
		super(ID);
		this.server = server;
	}

	public static MapWorkspaceManager get(MinecraftServer server) {
		ServerWorld overworld = server.overworld();
		return overworld.getDataStorage().computeIfAbsent(() -> new MapWorkspaceManager(server), ID);
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
		return RuntimeDimensions.get(server).getOrOpenPersistent(Util.resource(id), () -> {
			MapWorldInfo worldInfo = MapWorldInfo.create(server, mapSettings);
			return dimensionConfig.toRuntimeConfig(server, worldInfo);
		});
	}

	public boolean deleteWorkspace(String id) {
		MapWorkspace workspace = this.workspaces.remove(id);
		if (workspace != null) {
			workspace.getHandle().delete();
			return true;
		}
		return false;
	}

	@Nullable
	public MapWorkspace getWorkspace(String id) {
		return this.workspaces.get(id);
	}

	@Nullable
	public MapWorkspace getWorkspace(RegistryKey<World> dimension) {
		ResourceLocation name = dimension.location();
		if (!name.getNamespace().equals(Constants.MODID)) {
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

	public boolean isWorkspace(RegistryKey<World> dimension) {
		return getWorkspace(dimension) != null;
	}

	@Override
	public CompoundNBT save(CompoundNBT root) {
		ListNBT workspaceList = new ListNBT();

		WorldGenSettingsExport<INBT> ops = WorldGenSettingsExport.create(NBTDynamicOps.INSTANCE, this.server.registryAccess());

		for (Map.Entry<String, MapWorkspace> entry : this.workspaces.entrySet()) {
			MapWorkspaceData data = entry.getValue().intoData();
			MapWorkspaceData.CODEC.encodeStart(ops, data)
					.result().ifPresent(workspaceList::add);
		}

		root.put("workspaces", workspaceList);

		return root;
	}

	@Override
	public void load(CompoundNBT root) {
		this.workspaces.clear();

		ListNBT workspaceList = root.getList("workspaces", NBT.TAG_COMPOUND);

		DynamicOps<INBT> ops = DynamicRegistryReadingOps.create(this.server, NBTDynamicOps.INSTANCE);

		for (int i = 0; i < workspaceList.size(); i++) {
			CompoundNBT workspaceRoot = workspaceList.getCompound(i);

			DataResult<MapWorkspaceData> result = MapWorkspaceData.CODEC.parse(ops, workspaceRoot);
			result.result().ifPresent(workspaceData -> {
				RuntimeDimensionHandle dimensionHandle = getOrCreateDimension(workspaceData.id, workspaceData.dimension, workspaceData.worldSettings);
				MapWorkspace workspace = workspaceData.create(server, dimensionHandle);
				this.workspaces.put(workspaceData.id, workspace);
			});

			result.error().ifPresent(error -> {
				LoveTropics.LOGGER.warn("Failed to load map workspace: {}", error);
			});
		}
	}

	@Override
	public boolean isDirty() {
		return true;
	}
}
