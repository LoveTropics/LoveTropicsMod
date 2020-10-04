package com.lovetropics.minigames.common.map.workspace;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.map.MapWorldInfo;
import com.lovetropics.minigames.common.map.MapWorldSettings;
import com.lovetropics.minigames.common.map.generator.ConfiguredGenerator;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class MapWorkspaceManager extends WorldSavedData {
	private static final String ID = Constants.MODID + ":map_workspace_manager";

	private final MinecraftServer server;
	private final Map<String, MapWorkspace> workspaces = new Object2ObjectOpenHashMap<>();

	private MapWorkspaceManager(MinecraftServer server) {
		super(ID);
		this.server = server;
	}

	public static MapWorkspaceManager get(MinecraftServer server) {
		ServerWorld overworld = server.getWorld(DimensionType.OVERWORLD);
		return overworld.getSavedData().getOrCreate(() -> new MapWorkspaceManager(server), ID);
	}

	public MapWorkspace openWorkspace(String id, ConfiguredGenerator generator) {
		DimensionType dimension = getOrCreateDimension(id);

		ServerWorld overworld = server.getWorld(DimensionType.OVERWORLD);
		MapWorldSettings settings = MapWorldSettings.createFrom(overworld.getWorldInfo());

		MapWorkspace workspace = new MapWorkspace(id, dimension, generator, settings);
		this.workspaces.putIfAbsent(id, workspace);

		return workspace;
	}

	private DimensionType getOrCreateDimension(String id) {
		return DimensionManager.registerOrGetDimension(Util.resource(id), MapWorkspaceDimension.MOD_DIMENSION.get(), null, true);
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

	@Nullable
	public MapWorkspace getWorkspace(DimensionType dimension) {
		ResourceLocation name = dimension.getRegistryName();
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
			DimensionType dimension = getOrCreateDimension(id);

			MapWorkspace workspace = MapWorkspace.read(id, dimension, workspaceRoot);
			this.workspaces.put(id, workspace);
		}
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		World world = event.getWorld().getWorld();
		if (world.isRemote) {
			return;
		}

		MinecraftServer server = world.getServer();
		MapWorkspaceManager workspaceManager = get(server);

		MapWorkspace workspace = workspaceManager.getWorkspace(world.getDimension().getType());
		if (workspace == null) {
			return;
		}

		World overworld = server.getWorld(DimensionType.OVERWORLD);
		world.worldInfo = new MapWorldInfo(overworld.getWorldInfo(), workspace.getWorldSettings());
	}
}
