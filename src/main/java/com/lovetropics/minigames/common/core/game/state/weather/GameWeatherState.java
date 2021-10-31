package com.lovetropics.minigames.common.core.game.state.weather;

import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.lovetropics.minigames.common.core.game.weather.RainType;
import com.lovetropics.minigames.common.core.game.weather.WeatherController;

import javax.annotation.Nullable;

public final class GameWeatherState implements IGameState {
	public static final GameStateKey<GameWeatherState> KEY = GameStateKey.create("Weather State");

	private final WeatherController controller;

	private WeatherEvent event;

	public GameWeatherState(WeatherController controller) {
		this.controller = controller;
	}

	public void clear() {
		this.clearEvent();
		this.setWind(0.0F);
	}

	public void tick() {
		WeatherEvent event = this.event;
		if (event != null) {
			if (event.time-- <= 0) {
				this.clearEvent();
			}
		}
	}

	public void setEvent(WeatherEventType event, long length) {
		this.setEvent(new WeatherEvent(event, length));
	}

	public void setWind(float wind) {
		this.controller.setWind(wind);
	}

	public void clearEvent() {
		this.setEvent(null);
	}

	private void setEvent(@Nullable WeatherEvent event) {
		this.event = event;

		WeatherEventType type = event != null ? event.getType() : null;

		if (type == WeatherEventType.HEAVY_RAIN) {
			controller.setRain(1.0F, RainType.NORMAL);
		} else if (type == WeatherEventType.ACID_RAIN) {
			controller.setRain(1.0F, RainType.ACID);
		} else {
			controller.setRain(0.0F, controller.getRainType());
		}

		controller.setHeatwave(type == WeatherEventType.HEATWAVE);
		controller.setSandstorm(type == WeatherEventType.SANDSTORM);
		controller.setSnowstorm(type == WeatherEventType.SNOWSTORM);
	}

	@Nullable
	public WeatherEvent getEvent() {
		return event;
	}

	@Nullable
	public WeatherEventType getEventType() {
		return event != null ? event.getType() : null;
	}
}
