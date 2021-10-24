package com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.Map;

public final class PlantState {
	private final Map<Key<?>, Object> map = new Reference2ObjectOpenHashMap<>();

	public <S> void put(Key<S> key, S state) {
		this.map.put(key, state);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <S> S get(Key<S> key) {
		return (S) this.map.get(key);
	}

	public static final class Key<S> {
		private Key() {
		}

		public static <S> Key<S> create() {
			return new Key<>();
		}
	}
}
