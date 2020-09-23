package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.weather.IMinigameWeatherInstance;
import com.lovetropics.minigames.common.minigames.weather.MinigameWeatherConfig;

import com.mojang.datafixers.Dynamic;
import net.minecraft.world.World;

public class WeatherEventsMinigameBehavior implements IMinigameBehavior
{
	private IMinigameWeatherInstance<?> minigameWeatherInstance;
	private MinigameWeatherConfig config;

	public WeatherEventsMinigameBehavior(final MinigameWeatherConfig config) {
		this.minigameWeatherInstance = new IMinigameWeatherInstance.Noop<>(); // TODO new MinigameWeatherInstanceServer();
		this.config = config;
	}

	public static <T> WeatherEventsMinigameBehavior parse(Dynamic<T> root) {
		return new WeatherEventsMinigameBehavior(MinigameWeatherConfig.parse(root));
	}

	@Override
	public void worldUpdate(final IMinigameInstance minigame, World world) {
		if (world.getDimension().getType() == minigame.getDefinition().getDimension()) {
			// TODO: minigameWeatherInstance.tick(definition);
		}
	}

	@Override
	public void onFinish(final IMinigameInstance minigame) {
		minigameWeatherInstance.reset();
	}

	@Override
	public void onStart(final IMinigameInstance minigame) {
		minigameWeatherInstance.setMinigameActive(true);
	}
}
