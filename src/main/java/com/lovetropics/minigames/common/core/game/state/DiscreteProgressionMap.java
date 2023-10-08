package com.lovetropics.minigames.common.core.game.state;

import com.mojang.serialization.Codec;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

public class DiscreteProgressionMap<V> {
	private final Map<ProgressionPoint, V> values;

	public DiscreteProgressionMap(Map<ProgressionPoint, V> values) {
		this.values = values;
	}

	public static <V> Codec<DiscreteProgressionMap<V>> codec(Codec<V> codec) {
		return Codec.unboundedMap(ProgressionPoint.CODEC, codec).xmap(DiscreteProgressionMap::new, m -> m.values);
	}

	// TODO: Terribly inefficient
	@Nullable
	public V get(GameProgressionState progression) {
		int lastTime = Integer.MIN_VALUE;
		V lastValue = null;
		for (Map.Entry<ProgressionPoint, V> entry : values.entrySet()) {
			int time = entry.getKey().resolve(progression);
			if (time > lastTime && progression.time() >= time) {
				lastTime = time;
				lastValue = entry.getValue();
			}
		}
		return lastValue;
	}

	public V getOrDefault(GameProgressionState progression, V fallback) {
		return Objects.requireNonNullElse(get(progression), fallback);
	}
}
