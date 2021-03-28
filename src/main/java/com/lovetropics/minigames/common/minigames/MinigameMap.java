package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.map.MapRegions;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;

public final class MinigameMap {
	private final @Nullable String name;
	private final RegistryKey<World> dimension;
	private final MapRegions mapRegions;

	public MinigameMap(@Nullable String name, RegistryKey<World> dimension, MapRegions mapRegions) {
		this.name = name;
		this.dimension = dimension;
		this.mapRegions = mapRegions;
	}

	public MinigameMap(@Nullable String name, RegistryKey<World> dimension) {
		this(name, dimension, new MapRegions());
	}

	public Optional<String> getName() {
		return Optional.ofNullable(name);
	}

	public RegistryKey<World> getDimension() {
		return dimension;
	}

	public MapRegions getMapRegions() {
		return mapRegions;
	}

	public MinigameMap withName(String key) {
		return new MinigameMap(key, dimension, mapRegions);
	}
}
