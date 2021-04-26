package com.lovetropics.minigames.common.core.game.map;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.map.MapRegions;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public final class GameMap {
	private final @Nullable String name;
	private final RegistryKey<World> dimension;
	private final MapRegions mapRegions;
	private final Consumer<IActiveGame> close;

	private GameMap(@Nullable String name, RegistryKey<World> dimension, MapRegions mapRegions, @Nullable Consumer<IActiveGame> close) {
		this.name = name;
		this.dimension = dimension;
		this.mapRegions = mapRegions;
		this.close = close;
	}

	public GameMap(@Nullable String name, RegistryKey<World> dimension, MapRegions mapRegions) {
		this(name, dimension, mapRegions, null);
	}

	public GameMap(@Nullable String name, RegistryKey<World> dimension) {
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

	public GameMap withName(String key) {
		return new GameMap(key, dimension, mapRegions);
	}

	public GameMap onClose(Consumer<IActiveGame> close) {
		return new GameMap(name, dimension, mapRegions, close);
	}

	public void close(IActiveGame game) {
		if (this.close != null) {
			this.close.accept(game);
		}
	}
}
