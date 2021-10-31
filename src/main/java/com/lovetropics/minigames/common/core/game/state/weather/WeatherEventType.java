package com.lovetropics.minigames.common.core.game.state.weather;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;

public enum WeatherEventType {
	HEAVY_RAIN("heavy_rain"),
	ACID_RAIN("acid_rain"),
	HEATWAVE("heatwave"),
	SANDSTORM("sandstorm"),
	SNOWSTORM("snowstorm");

	public static final Codec<WeatherEventType> CODEC = MoreCodecs.stringVariants(values(), WeatherEventType::getKey);

	private final String key;

	WeatherEventType(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
