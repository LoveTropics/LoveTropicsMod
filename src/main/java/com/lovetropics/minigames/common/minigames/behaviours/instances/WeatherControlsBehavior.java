package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.weather.RainType;
import com.lovetropics.minigames.common.minigames.weather.WeatherController;
import com.lovetropics.minigames.common.minigames.weather.WeatherControllerManager;
import com.mojang.datafixers.Dynamic;

public class WeatherControlsBehavior implements IMinigameBehavior {
	private WeatherController controller;

	public static <T> WeatherControlsBehavior parse(Dynamic<T> root) {
		return new WeatherControlsBehavior();
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		controller = WeatherControllerManager.forWorld(minigame.getWorld());

		minigame.addControlCommand("start_heatwave", source -> controller.setHeatwave(true));
		minigame.addControlCommand("stop_heatwave", source -> controller.setHeatwave(false));

		minigame.addControlCommand("start_rain", source -> controller.setRain(1.0F, RainType.NORMAL));
		minigame.addControlCommand("stop_rain", source -> controller.setRain(0.0F, RainType.NORMAL));

		minigame.addControlCommand("start_acid_rain", source -> controller.setRain(1.0F, RainType.ACID));
		minigame.addControlCommand("stop_acid_rain", source -> controller.setRain(0.0F, RainType.ACID));

		minigame.addControlCommand("start_wind", source -> controller.setWind(0.5F));
		minigame.addControlCommand("stop_wind", source -> controller.setWind(0.0F));
	}

	@Override
	public void onFinish(final IMinigameInstance minigame) {
		controller.reset();
	}
}
