package com.lovetropics.minigames.common.core.game.weather;

import net.minecraft.entity.player.ServerPlayerEntity;

public interface WeatherController {
	void onPlayerJoin(ServerPlayerEntity player);

	void tick();

	void setRain(float amount, RainType type);

	void setWind(float speed);

	void setHeatwave(boolean heatwave);

	float getRainAmount();

	RainType getRainType();

	float getWindSpeed();

	boolean isHeatwave();

	default void reset() {
		setRain(0.0F, RainType.NORMAL);
		setWind(0.0F);
		setHeatwave(false);
	}
}
