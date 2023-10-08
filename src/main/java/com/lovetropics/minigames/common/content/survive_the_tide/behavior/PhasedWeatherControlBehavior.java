package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.GameProgressionState;
import com.lovetropics.minigames.common.core.game.state.ProgressionPeriod;
import com.lovetropics.minigames.common.core.game.state.weather.GameWeatherState;
import com.lovetropics.minigames.common.core.game.weather.WeatherEvent;
import com.lovetropics.minigames.common.core.game.weather.WeatherEventType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class PhasedWeatherControlBehavior implements IGameBehavior {
	public static final Codec<PhasedWeatherControlBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			Period.CODEC.listOf().fieldOf("periods").forGetter(b -> b.periods)
	).apply(i, PhasedWeatherControlBehavior::new));

	private final List<Period> periods;

	public PhasedWeatherControlBehavior(final List<Period> periods) {
		this.periods = periods;
	}

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		final GameWeatherState weather = game.getState().getOrThrow(GameWeatherState.KEY);
		final GameProgressionState progression = game.getState().getOrThrow(GameProgressionState.KEY);

		events.listen(GamePhaseEvents.TICK, () -> {
			final ServerLevel level = game.getWorld();
			if (level.getGameTime() % SharedConstants.TICKS_PER_SECOND == 0) {
				tick(weather, progression);
			}
		});
	}

	private void tick(final GameWeatherState weather, final GameProgressionState progression) {
		final WeatherEventType weatherType = getCurrentWeather(progression);
		if (Objects.equals(weatherType, weather.getEventType())) {
			return;
		}
		if (weatherType != null) {
			weather.setEvent(WeatherEvent.createGeneric(weatherType, Long.MAX_VALUE));
		} else {
			weather.setEvent(null);
		}
	}

	@Nullable
	private WeatherEventType getCurrentWeather(final GameProgressionState progression) {
		for (final Period period : periods) {
			if (progression.is(period.period())) {
				return period.weather();
			}
		}
		return null;
	}

	public record Period(ProgressionPeriod period, WeatherEventType weather) {
		public static final Codec<Period> CODEC = RecordCodecBuilder.create(i -> i.group(
				ProgressionPeriod.MAP_CODEC.forGetter(Period::period),
				WeatherEventType.CODEC.fieldOf("weather").forGetter(Period::weather)
		).apply(i, Period::new));
	}
}
