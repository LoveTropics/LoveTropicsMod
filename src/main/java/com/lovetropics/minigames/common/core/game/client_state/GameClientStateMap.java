package com.lovetropics.minigames.common.core.game.client_state;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;

public final class GameClientStateMap implements Iterable<GameClientState> {
	private final Map<GameClientStateType<?>, GameClientState> map = new Reference2ObjectOpenHashMap<>();

	public static GameClientStateMap empty() {
		return new GameClientStateMap();
	}

	public <T extends GameClientState> void add(T tweak) {
		this.map.put(tweak.getType(), tweak);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends GameClientState> T getOrNull(GameClientStateType<T> type) {
		return (T) this.map.get(type);
	}

	@SuppressWarnings("unchecked")
	@Nullable
	public <T extends GameClientState> T remove(GameClientStateType<T> type) {
		return (T) this.map.remove(type);
	}

	public boolean isEmpty() {
		return this.map.isEmpty();
	}

	@Override
	public Iterator<GameClientState> iterator() {
		return this.map.values().iterator();
	}
}
