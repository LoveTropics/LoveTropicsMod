package com.lovetropics.minigames.common.minigames;

import java.util.Optional;

import javax.annotation.Nullable;

import com.lovetropics.minigames.common.map.MapRegions;
import net.minecraft.world.dimension.DimensionType;

public final class MinigameMap {
	private final @Nullable String name;
	private final DimensionType dimension;
	private final MapRegions mapRegions;

	public MinigameMap(@Nullable String name, DimensionType dimension, MapRegions mapRegions) {
		this.name = name;
		this.dimension = dimension;
		this.mapRegions = mapRegions;
	}

	public MinigameMap(@Nullable String name, DimensionType dimension) {
		this(name, dimension, new MapRegions());
	}

	public Optional<String> getName() {
		return Optional.ofNullable(name);
	}

	public DimensionType getDimension() {
		return dimension;
	}

	public MapRegions getMapRegions() {
		return mapRegions;
	}

	public MinigameMap withName(String key) {
		return new MinigameMap(key, dimension, mapRegions);
	}
}
