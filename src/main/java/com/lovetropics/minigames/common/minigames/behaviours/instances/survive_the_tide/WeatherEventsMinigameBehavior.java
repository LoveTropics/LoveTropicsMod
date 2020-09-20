package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.weather.IMinigameWeatherInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.weather.MinigameWeatherConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.world.World;

public class WeatherEventsMinigameBehavior implements IMinigameBehavior
{
	private IMinigameWeatherInstance minigameWeatherInstance;
	private MinigameWeatherConfig config;

	public WeatherEventsMinigameBehavior(final MinigameWeatherConfig config) {
		this.minigameWeatherInstance = new IMinigameWeatherInstance.Noop<>(); // TODO new MinigameWeatherInstanceServer();
		this.config = config;
	}

	@Override
	public void worldUpdate(final IMinigameDefinition definition, World world, IMinigameInstance instance) {
		if (world.getDimension().getType() == definition.getDimension()) {
			// TODO: minigameWeatherInstance.tick(definition);
		}
	}

	@Override
	public void onFinish(final IMinigameDefinition definition, CommandSource commandSource, IMinigameInstance instance) {
		minigameWeatherInstance.reset();
	}

	@Override
	public void onStart(final IMinigameDefinition definition, CommandSource commandSource, IMinigameInstance instance) {
		minigameWeatherInstance.setMinigameActive(true);
	}
}
