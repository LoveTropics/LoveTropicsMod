package com.lovetropics.minigames.common.map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.dimension.DimensionType;

public final class MapWorkspace {
	private final String id;
	private final DimensionType dimension;

	private final MapRegionSet regions = new MapRegionSet();

	public MapWorkspace(String id, DimensionType dimension) {
		this.id = id;
		this.dimension = dimension;
	}

	public String getId() {
		return id;
	}

	public DimensionType getDimension() {
		return dimension;
	}

	public MapRegionSet getRegions() {
		return regions;
	}

	public void write(CompoundNBT root) {
		root.put("regions", regions.write(new CompoundNBT()));
	}

	public void read(CompoundNBT root) {
		regions.read(root.getCompound("regions"));
	}
}
