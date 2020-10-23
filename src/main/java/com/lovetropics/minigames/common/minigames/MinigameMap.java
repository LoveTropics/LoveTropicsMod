package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.map.MapRegions;
import net.minecraft.world.dimension.DimensionType;

public final class MinigameMap {
	private final DimensionType dimension;
	private final MapRegions mapRegions;

	public MinigameMap(DimensionType dimension, MapRegions mapRegions) {
		this.dimension = dimension;
		this.mapRegions = mapRegions;
	}

	public MinigameMap(DimensionType dimension) {
		this(dimension, new MapRegions());
	}

	public DimensionType getDimension() {
		return dimension;
	}

	public MapRegions getMapRegions() {
		return mapRegions;
	}
}
