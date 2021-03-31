package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.minigames.common.core.game.ControlCommand;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.weather.RainType;
import com.lovetropics.minigames.common.core.game.weather.WeatherController;
import com.lovetropics.minigames.common.core.game.weather.WeatherControllerManager;
import com.mojang.serialization.Codec;

public class WeatherControlsBehavior implements IGameBehavior {
	public static final Codec<WeatherControlsBehavior> CODEC = Codec.unit(WeatherControlsBehavior::new);

	private WeatherController controller;

	@Override
	public void register(IGameInstance registerGame, EventRegistrar events) {
		controller = WeatherControllerManager.forWorld(registerGame.getWorld());

		events.listen(GameLifecycleEvents.FINISH, g -> controller.reset());

		registerGame.addControlCommand("start_heatwave", ControlCommand.forAdmins(source -> controller.setHeatwave(true)));
		registerGame.addControlCommand("stop_heatwave", ControlCommand.forAdmins(source -> controller.setHeatwave(false)));

		registerGame.addControlCommand("start_rain", ControlCommand.forAdmins(source -> controller.setRain(1.0F, RainType.NORMAL)));
		registerGame.addControlCommand("stop_rain", ControlCommand.forAdmins(source -> controller.setRain(0.0F, RainType.NORMAL)));

		registerGame.addControlCommand("start_acid_rain", ControlCommand.forAdmins(source -> controller.setRain(1.0F, RainType.ACID)));
		registerGame.addControlCommand("stop_acid_rain", ControlCommand.forAdmins(source -> controller.setRain(0.0F, RainType.ACID)));

		registerGame.addControlCommand("start_wind", ControlCommand.forAdmins(source -> controller.setWind(0.5F)));
		registerGame.addControlCommand("stop_wind", ControlCommand.forAdmins(source -> controller.setWind(0.0F)));
	}
}
