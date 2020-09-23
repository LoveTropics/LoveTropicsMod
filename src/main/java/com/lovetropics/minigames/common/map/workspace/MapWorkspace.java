package com.lovetropics.minigames.common.map.workspace;

import com.lovetropics.minigames.common.map.MapWorldSettings;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.dimension.DimensionType;

public final class MapWorkspace {
	private final String id;
	private final DimensionType dimension;

	private final MapWorldSettings worldSettings;

	private final WorkspaceRegions regions;

	MapWorkspace(String id, DimensionType dimension, MapWorldSettings worldSettings) {
		this.regions = new WorkspaceRegions(dimension);
		this.id = id;
		this.dimension = dimension;
		this.worldSettings = worldSettings;
	}

	public String getId() {
		return id;
	}

	public DimensionType getDimension() {
		return dimension;
	}

	public WorkspaceRegions getRegions() {
		return regions;
	}

	public MapWorldSettings getWorldSettings() {
		return worldSettings;
	}

	public void write(CompoundNBT root) {
		root.put("regions", regions.write(new CompoundNBT()));
		root.put("settings", worldSettings.write(new CompoundNBT()));
	}

	public void read(CompoundNBT root) {
		regions.read(root.getCompound("regions"));
		worldSettings.read(root.getCompound("settings"));
	}
}
