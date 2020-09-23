package com.lovetropics.minigames.common.map;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.Util;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;
import java.util.Map;

public final class MapWorkspaceManager extends WorldSavedData {
	private static final String ID = Constants.MODID + ":map_workspace_manager";

	private final Map<String, MapWorkspace> workspaces = new Object2ObjectOpenHashMap<>();

	private MapWorkspaceManager() {
		super(ID);
	}

	public static MapWorkspaceManager get(MinecraftServer server) {
		ServerWorld overworld = server.getWorld(DimensionType.OVERWORLD);
		return overworld.getSavedData().getOrCreate(MapWorkspaceManager::new, ID);
	}

	public MapWorkspace openWorkspace(String id) {
		DimensionType dimension = DimensionManager.registerOrGetDimension(Util.resource(id), MapWorkspaceDimension.MOD_DIMENSION.get(), null, true);

		MapWorkspace workspace = new MapWorkspace(id, dimension);
		this.workspaces.putIfAbsent(id, workspace);

		return workspace;
	}

	public boolean deleteWorkspace(String id) {
		MapWorkspace workspace = this.workspaces.remove(id);
		if (workspace != null) {
			DimensionManager.markForDeletion(workspace.getDimension());
			return true;
		}
		return false;
	}

	@Nullable
	public MapWorkspace getWorkspace(String id) {
		return this.workspaces.get(id);
	}

	public boolean hasWorkspace(String id) {
		return this.workspaces.containsKey(id);
	}

	@Override
	public CompoundNBT write(CompoundNBT root) {
		ListNBT workspaceList = new ListNBT();

		for (Map.Entry<String, MapWorkspace> entry : this.workspaces.entrySet()) {
			CompoundNBT workspaceRoot = new CompoundNBT();

			workspaceRoot.putString("id", entry.getKey());
			entry.getValue().write(workspaceRoot);

			workspaceList.add(workspaceRoot);
		}

		root.put("workspaces", workspaceList);

		return root;
	}

	@Override
	public void read(CompoundNBT root) {
		this.workspaces.clear();

		ListNBT workspaceList = root.getList("workspaces", NBT.TAG_COMPOUND);

		for (int i = 0; i < workspaceList.size(); i++) {
			CompoundNBT workspaceRoot = workspaceList.getCompound(i);

			String id = workspaceRoot.getString("id");
			MapWorkspace workspace = openWorkspace(id);

			workspace.read(workspaceRoot);
		}
	}

	@Override
	public boolean isDirty() {
		return true;
	}
}
