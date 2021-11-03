package com.lovetropics.minigames.common.core.game.state.weather;

import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.lovetropics.minigames.common.core.game.weather.WeatherController;
import com.lovetropics.minigames.common.core.game.weather.WeatherEvent;
import com.lovetropics.minigames.common.core.game.weather.WeatherEventType;

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
		if (event != null && event.tick() == WeatherEvent.TickResult.STOP) {
			this.clearEvent();
		}
	}

	public void setWind(float wind) {
		this.controller.setWind(wind);
	}

	public void setEvent(@Nullable WeatherEvent event) {
		WeatherEvent lastEvent = this.event;
		if (lastEvent != null) {
			lastEvent.remove(controller);
		}

		this.event = event;

		if (event != null) {
			event.apply(controller);
		}
	}

	public void clearEvent() {
		this.setEvent(null);
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
