package com.lovetropics.minigames.common.core.game.client_tweak;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.Map;

public final class GameClientTweakMap {
	private final Map<GameClientTweakType<?>, GameClientTweak> map = new Reference2ObjectOpenHashMap<>();

	public static GameClientTweakMap empty() {
		return new GameClientTweakMap();
	}

	public <T extends GameClientTweak> void add(T tweak) {
		this.map.put(tweak.getType(), tweak);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends GameClientTweak> T getOrNull(GameClientTweakType<T> type) {
		return (T) this.map.get(type);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends GameClientTweak> T remove(GameClientTweakType<T> type) {
		return (T) this.map.remove(type);
	}

	public boolean isEmpty() {
		return this.map.isEmpty();
	}
}
