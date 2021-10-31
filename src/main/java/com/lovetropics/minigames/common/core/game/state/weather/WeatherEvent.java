package com.lovetropics.minigames.common.core.game.state.weather;

public final class WeatherEvent {
	final WeatherEventType type;
	long time;

	public WeatherEvent(WeatherEventType type, long length) {
		this.type = type;
		this.time = length;
	}

	public WeatherEventType getType() {
		return type;
	}

	public long getTime() {
		return time;
	}
}
