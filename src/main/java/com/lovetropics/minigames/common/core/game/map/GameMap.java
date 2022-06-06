package com.lovetropics.minigames.common.core.game.map;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.map.MapRegions;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public record GameMap(@Nullable String name, ResourceKey<Level> dimension, MapRegions mapRegions, @Nullable Consumer<IGamePhase> close) {
	public GameMap(@Nullable String name, ResourceKey<Level> dimension, MapRegions mapRegions) {
		this(name, dimension, mapRegions, null);
	}

	public GameMap(@Nullable String name, ResourceKey<Level> dimension) {
		this(name, dimension, new MapRegions());
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
