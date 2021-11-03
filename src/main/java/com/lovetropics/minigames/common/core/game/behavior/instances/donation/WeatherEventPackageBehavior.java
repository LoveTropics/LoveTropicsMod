package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.state.weather.GameWeatherState;
import com.lovetropics.minigames.common.core.game.weather.WeatherEvent;
import com.lovetropics.minigames.common.core.game.weather.WeatherEventType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import javax.annotation.Nullable;

public final class WeatherEventPackageBehavior implements IGameBehavior {
	public static final Codec<WeatherEventPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				WeatherEventType.CODEC.fieldOf("event").forGetter(c -> c.type),
				Codec.LONG.fieldOf("seconds").forGetter(c -> c.ticks / 20)
		).apply(instance, WeatherEventPackageBehavior::new);
	});

	public final WeatherEventType type;
	public final long ticks;

	private GameWeatherState weather;

	public WeatherEventPackageBehavior(WeatherEventType type, long seconds) {
		this.type = type;
		this.ticks = seconds * 20;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		weather = game.getState().getOrThrow(GameWeatherState.KEY);

		events.listen(GamePackageEvents.APPLY_PACKAGE, (player, sendingPlayer) -> {
			WeatherEvent event = this.tryCreateEvent(ticks);
			if (event != null) {
				weather.setEvent(event);
				return true;
			} else {
				return false;
			}
		});
	}

	@Nullable
	private WeatherEvent tryCreateEvent(long time) {
		switch (this.type) {
			case HEAVY_RAIN: return WeatherEvent.heavyRain(time);
			case ACID_RAIN: return WeatherEvent.acidRain(time);
			case HEATWAVE: return WeatherEvent.heatwave(time);
			default: return null;
		}
	}
}
