package com.lovetropics.minigames.common.core.game.weather;

import com.lovetropics.minigames.common.content.survive_the_tide.SurviveTheTideWeatherConfig;
import net.minecraft.entity.player.ServerPlayerEntity;

public interface WeatherController {
	void onPlayerJoin(ServerPlayerEntity player);

	void tick();

	void setRain(float amount, RainType type);

	void setWind(float speed);

	void setHeatwave(boolean heatwave);

	void setSandstorm(boolean sandstorm);

	void setSnowstorm(boolean snowstorm);

	float getRainAmount();

	RainType getRainType();

	float getWindSpeed();

	boolean isHeatwave();

	boolean isSandstorm();

	boolean isSnowstorm();

	default void reset() {
		setRain(0.0F, RainType.NORMAL);
		setWind(0.0F);
		setHeatwave(false);
		setSandstorm(false);
		setSnowstorm(false);
	}

	void setConfig(SurviveTheTideWeatherConfig config);

	SurviveTheTideWeatherConfig getConfig();
}
