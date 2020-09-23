package com.lovetropics.minigames.common.map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.dimension.DimensionType;

public final class MapWorkspace {
	private final String id;
	private final DimensionType dimension;

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

	public void write(CompoundNBT root) {

	}

	public void read(CompoundNBT root) {

	}
}
