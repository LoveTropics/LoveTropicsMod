package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.ControlCommand;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.weather.RainType;
import com.lovetropics.minigames.common.core.game.weather.WeatherController;
import com.lovetropics.minigames.common.core.game.weather.WeatherControllerManager;
import com.mojang.serialization.Codec;

public class WeatherControlsBehavior implements IGameBehavior {
	public static final Codec<WeatherControlsBehavior> CODEC = Codec.unit(WeatherControlsBehavior::new);

	private WeatherController controller;

	@Override
	public void onConstruct(IGameInstance minigame) {
		controller = WeatherControllerManager.forWorld(minigame.getWorld());

		minigame.addControlCommand("start_heatwave", ControlCommand.forAdmins(source -> controller.setHeatwave(true)));
		minigame.addControlCommand("stop_heatwave", ControlCommand.forAdmins(source -> controller.setHeatwave(false)));

		minigame.addControlCommand("start_rain", ControlCommand.forAdmins(source -> controller.setRain(1.0F, RainType.NORMAL)));
		minigame.addControlCommand("stop_rain", ControlCommand.forAdmins(source -> controller.setRain(0.0F, RainType.NORMAL)));

		minigame.addControlCommand("start_acid_rain", ControlCommand.forAdmins(source -> controller.setRain(1.0F, RainType.ACID)));
		minigame.addControlCommand("stop_acid_rain", ControlCommand.forAdmins(source -> controller.setRain(0.0F, RainType.ACID)));

		minigame.addControlCommand("start_wind", ControlCommand.forAdmins(source -> controller.setWind(0.5F)));
		minigame.addControlCommand("stop_wind", ControlCommand.forAdmins(source -> controller.setWind(0.0F)));
	}

	@Override
	public void onFinish(final IGameInstance minigame) {
		controller.reset();
	}
}
