package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.weather.IMinigameWeatherInstance;
import com.lovetropics.minigames.common.minigames.weather.MinigameWeatherConfig;
import com.mojang.datafixers.Dynamic;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class WeatherEventsMinigameBehavior implements IMinigameBehavior
{
	@Nonnull
	public static Supplier<? extends IMinigameWeatherInstance> WEATHER_IMPL = IMinigameWeatherInstance.Noop::new;

	private IMinigameWeatherInstance minigameWeatherInstance;
	private MinigameWeatherConfig config;

	public WeatherEventsMinigameBehavior(final MinigameWeatherConfig config) {
		this.minigameWeatherInstance = WEATHER_IMPL.get();
		this.config = config;
	}

	public static <T> WeatherEventsMinigameBehavior parse(Dynamic<T> root) {
		return new WeatherEventsMinigameBehavior(MinigameWeatherConfig.parse(root));
	}
	
	public IMinigameWeatherInstance getMinigameWeatherInstance() {
		return minigameWeatherInstance;
	}
	
	public MinigameWeatherConfig getConfig() {
		return config;
	}

	@Override
	public void worldUpdate(final IMinigameInstance minigame, World world) {
		minigameWeatherInstance.tick(minigame);
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
