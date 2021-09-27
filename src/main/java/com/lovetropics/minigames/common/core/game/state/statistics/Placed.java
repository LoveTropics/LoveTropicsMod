package com.lovetropics.minigames.common.core.game.state.statistics;

import java.util.Objects;

public final class Placed<T> implements Comparable<Placed<T>> {
	public final int placement;
	public final T value;

	Placed(int placement, T value) {
		this.placement = placement;
		this.value = value;
	}

	public static <T> Placed<T> at(int placement, T value) {
		return new Placed<>(placement, value);
	}

	@Override
	public int compareTo(Placed<T> other) {
		return Integer.compare(placement, other.placement);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;

		if (obj instanceof Placed<?>) {
			Placed<?> placed = (Placed<?>) obj;
			return placement == placed.placement && Objects.equals(value, placed.value);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return value.hashCode() + placement * 31;
	}
}
