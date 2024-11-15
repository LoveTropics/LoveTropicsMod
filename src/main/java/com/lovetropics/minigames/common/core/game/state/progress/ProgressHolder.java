package com.lovetropics.minigames.common.core.game.state.progress;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public final class ProgressHolder implements ProgressionPoint.NamedResolver {
	public static final int NOT_STARTED_TIME = -1;

	private final Object2IntMap<String> namedPoints = new Object2IntOpenHashMap<>();
	private int time = NOT_STARTED_TIME;

	public void addNamedPoint(String name, int value) {
		namedPoints.put(name, value);
	}

	public void start() {
		time = 0;
	}

	public void set(int time) {
		this.time = time;
	}

	public int time() {
		return time;
	}

	public boolean isStarted() {
		return time != NOT_STARTED_TIME;
	}

	public boolean is(ProgressionPeriod period) {
		return isAfter(period.start()) && isBefore(period.end());
	}

	public boolean is(Iterable<ProgressionPeriod> periods) {
		for (ProgressionPeriod period : periods) {
			if (is(period)) {
				return true;
			}
		}
		return false;
	}

	public float progressIn(ProgressionPeriod period) {
		int start = period.start().resolve(this);
		int end = period.end().resolve(this);
		if (start == ProgressionPoint.UNRESOLVED || end == ProgressionPoint.UNRESOLVED) {
			return 0.0f;
		}
		if (time <= start) {
			return 0.0f;
		} else if (time >= end) {
			return 1.0f;
		}
		return (float) (time - start) / (end - start);
	}

	public boolean isBefore(ProgressionPoint point) {
		int value = point.resolve(this);
		return value != ProgressionPoint.UNRESOLVED && time <= value;
	}

	public boolean isAfter(ProgressionPoint point) {
		int value = point.resolve(this);
		return value != ProgressionPoint.UNRESOLVED && time >= value;
	}

	@Override
	public int getNamedPoint(String name) {
		return namedPoints.getOrDefault(name, ProgressionPoint.UNRESOLVED);
	}
}
