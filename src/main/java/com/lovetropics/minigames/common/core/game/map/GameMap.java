package com.lovetropics.minigames.common.core.game.map;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.map.MapRegions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public final class GameMap {
	private final @Nullable String name;
	private final ResourceKey<Level> dimension;
	private final MapRegions mapRegions;
	private final Consumer<IGamePhase> close;

	private GameMap(@Nullable String name, ResourceKey<Level> dimension, MapRegions mapRegions, @Nullable Consumer<IGamePhase> close) {
		this.name = name;
		this.dimension = dimension;
		this.mapRegions = mapRegions;
		this.close = close;
	}

	public GameMap(@Nullable String name, ResourceKey<Level> dimension, MapRegions mapRegions) {
		this(name, dimension, mapRegions, null);
	}

	public GameMap(@Nullable String name, ResourceKey<Level> dimension) {
		this(name, dimension, new MapRegions());
	}

	public Optional<String> getName() {
		return Optional.ofNullable(name);
	}

	public ResourceKey<Level> getDimension() {
		return dimension;
	}

	public MapRegions getRegions() {
		return mapRegions;
	}

	public GameMap withName(String key) {
		return new GameMap(key, dimension, mapRegions);
	}

	public GameMap onClose(Consumer<IGamePhase> close) {
		return new GameMap(name, dimension, mapRegions, close);
	}

	public void close(IGamePhase game) {
		if (this.close != null) {
			this.close.accept(game);
		}
	}
}
