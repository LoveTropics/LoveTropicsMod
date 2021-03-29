package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.map.MapRegions;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public final class MinigameMap {
	private final @Nullable String name;
	private final RegistryKey<World> dimension;
	private final MapRegions mapRegions;
	private final Consumer<IMinigameInstance> close;

	private MinigameMap(@Nullable String name, RegistryKey<World> dimension, MapRegions mapRegions, @Nullable Consumer<IMinigameInstance> close) {
		this.name = name;
		this.dimension = dimension;
		this.mapRegions = mapRegions;
		this.close = close;
	}

	public MinigameMap(@Nullable String name, RegistryKey<World> dimension, MapRegions mapRegions) {
		this(name, dimension, mapRegions, null);
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

	public MapRegions getRegions() {
		return mapRegions;
	}

	public MinigameMap withName(String key) {
		return new MinigameMap(key, dimension, mapRegions);
	}

	public MinigameMap onClose(Consumer<IMinigameInstance> close) {
		return new MinigameMap(name, dimension, mapRegions, close);
	}

	public void close(IMinigameInstance minigame) {
		if (this.close != null) {
			this.close.accept(minigame);
		}
	}
}
