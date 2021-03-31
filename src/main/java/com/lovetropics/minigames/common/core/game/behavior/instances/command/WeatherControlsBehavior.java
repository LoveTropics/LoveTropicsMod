package com.lovetropics.minigames.common.core.game.behavior.instances.command;

import com.lovetropics.minigames.common.core.game.control.ControlCommand;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.control.GameControlCommands;
import com.lovetropics.minigames.common.core.game.weather.RainType;
import com.lovetropics.minigames.common.core.game.weather.WeatherController;
import com.lovetropics.minigames.common.core.game.weather.WeatherControllerManager;
import com.mojang.serialization.Codec;

public class WeatherControlsBehavior implements IGameBehavior {
	public static final Codec<WeatherControlsBehavior> CODEC = Codec.unit(WeatherControlsBehavior::new);

	private WeatherController controller;

	@Override
	public void register(IGameInstance game, EventRegistrar events) {
		controller = WeatherControllerManager.forWorld(game.getWorld());

		events.listen(GameLifecycleEvents.FINISH, g -> controller.reset());

		GameControlCommands controls = game.getControlCommands();
		controls.add("start_heatwave", ControlCommand.forAdmins(source -> controller.setHeatwave(true)));
		controls.add("stop_heatwave", ControlCommand.forAdmins(source -> controller.setHeatwave(false)));

		controls.add("start_rain", ControlCommand.forAdmins(source -> controller.setRain(1.0F, RainType.NORMAL)));
		controls.add("stop_rain", ControlCommand.forAdmins(source -> controller.setRain(0.0F, RainType.NORMAL)));

		controls.add("start_acid_rain", ControlCommand.forAdmins(source -> controller.setRain(1.0F, RainType.ACID)));
		controls.add("stop_acid_rain", ControlCommand.forAdmins(source -> controller.setRain(0.0F, RainType.ACID)));

		controls.add("start_wind", ControlCommand.forAdmins(source -> controller.setWind(0.5F)));
		controls.add("stop_wind", ControlCommand.forAdmins(source -> controller.setWind(0.0F)));
	}
}
