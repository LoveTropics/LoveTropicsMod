package com.lovetropics.minigames.common.content.survive_the_tide;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.state.GameProgressionState;
import com.lovetropics.minigames.common.core.game.state.ProgressionPeriod;
import com.lovetropics.minigames.common.core.game.state.ProgressionPoint;
import com.lovetropics.minigames.common.core.game.weather.WeatherEventType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;

import java.util.Map;

public class SurviveTheTideWeatherConfig {
	public static final Codec<SurviveTheTideWeatherConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.unboundedMap(WeatherEventType.CODEC, MoreCodecs.object2Float(ProgressionPoint.CODEC)).fieldOf("event_chances").forGetter(c -> c.eventChancesByTime),
				Codec.unboundedMap(WeatherEventType.CODEC, Timers.CODEC).fieldOf("event_timers").forGetter(c -> c.eventTimers),
				MoreCodecs.object2Float(ProgressionPoint.CODEC).fieldOf("wind_speed").forGetter(c -> c.windSpeedByTime),
				ProgressionPeriod.CODEC.fieldOf("halve_event_time").forGetter(c -> c.halveEventTime),
				Codec.INT.optionalFieldOf("sandstorm_buildup_tickrate", 40).forGetter(c -> c.sandstormBuildupTickRate),
				Codec.INT.optionalFieldOf("sandstorm_max_stackable", 1).forGetter(c -> c.sandstormMaxStackable),
				Codec.INT.optionalFieldOf("snowstorm_buildup_tickrate", 40).forGetter(c -> c.snowstormBuildupTickRate),
				Codec.INT.optionalFieldOf("snowstorm_max_stackable", 1).forGetter(c -> c.snowstormMaxStackable)
		).apply(instance, SurviveTheTideWeatherConfig::new);
	});

	private final Map<WeatherEventType, Object2FloatMap<ProgressionPoint>> eventChancesByTime;
	private final Map<WeatherEventType, Timers> eventTimers;

	private final Object2FloatMap<ProgressionPoint> windSpeedByTime;

	private final ProgressionPeriod halveEventTime;

	private final int sandstormBuildupTickRate;
	private final int sandstormMaxStackable;

	private final int snowstormBuildupTickRate;
	private final int snowstormMaxStackable;

	public SurviveTheTideWeatherConfig(
			Map<WeatherEventType, Object2FloatMap<ProgressionPoint>> eventChancesByTime,
			Map<WeatherEventType, Timers> eventTimers,
			Object2FloatMap<ProgressionPoint> windSpeedByTime,
			ProgressionPeriod halveEventTime,
			final int sandstormBuildupTickRate, final int sandstormMaxStackable,
			final int snowstormBuildupTickRate, final int snowstormMaxStackable
	) {
		this.eventChancesByTime = eventChancesByTime;
		this.eventTimers = eventTimers;
		this.windSpeedByTime = windSpeedByTime;
		this.halveEventTime = halveEventTime;

		this.sandstormBuildupTickRate = sandstormBuildupTickRate;
		this.sandstormMaxStackable = sandstormMaxStackable;

		this.snowstormBuildupTickRate = snowstormBuildupTickRate;
		this.snowstormMaxStackable = snowstormMaxStackable;
	}

	private float getEventChance(WeatherEventType event, GameProgressionState progression) {
		Object2FloatMap<ProgressionPoint> chances = this.eventChancesByTime.get(event);
		if (chances == null) {
			return 0.0f;
		}
		return findValue(progression, chances);
	}

	// TODO: Terribly inefficient
	private static float findValue(GameProgressionState progression, Object2FloatMap<ProgressionPoint> values) {
		int lastTime = 0;
		float lastChance = 0.0f;
		for (Object2FloatMap.Entry<ProgressionPoint> entry : Object2FloatMaps.fastIterable(values)) {
			int time = entry.getKey().resolve(progression);
			if (time > lastTime && progression.time() >= time) {
				lastTime = time;
				lastChance = entry.getFloatValue();
			}
		}
		return lastChance;
	}

	private Timers getTimers(WeatherEventType event) {
		return eventTimers.getOrDefault(event, Timers.DEFAULT);
	}

	public float getRainHeavyChance(GameProgressionState progression) {
		return getEventChance(WeatherEventType.HEAVY_RAIN, progression);
	}

	public float getRainAcidChance(GameProgressionState progression) {
		return getEventChance(WeatherEventType.ACID_RAIN, progression);
	}

	public float getHailChance(GameProgressionState progression) {
		return getEventChance(WeatherEventType.HAIL, progression);
	}

	public float getHeatwaveChance(GameProgressionState progression) {
		return getEventChance(WeatherEventType.HEATWAVE, progression);
	}

	public float getSandstormChance(GameProgressionState progression) {
		return getEventChance(WeatherEventType.SANDSTORM, progression);
	}

	public float getSnowstormChance(GameProgressionState progression) {
		return getEventChance(WeatherEventType.SNOWSTORM, progression);
	}

	public float getWindSpeed(GameProgressionState progression) {
		return findValue(progression, windSpeedByTime);
	}

	public int getRainHeavyMinTime() {
		return getTimers(WeatherEventType.HEAVY_RAIN).minTime;
	}

	public int getRainHeavyExtraRandTime() {
		return getTimers(WeatherEventType.HEAVY_RAIN).extraRandTime;
	}

	public int getHeatwaveMinTime() {
		return getTimers(WeatherEventType.HEATWAVE).minTime;
	}

	public int getHeatwaveExtraRandTime() {
		return getTimers(WeatherEventType.HEATWAVE).extraRandTime;
	}

	public int getRainAcidMinTime() {
		return getTimers(WeatherEventType.ACID_RAIN).minTime;
	}

	public int getRainAcidExtraRandTime() {
		return getTimers(WeatherEventType.ACID_RAIN).extraRandTime;
	}

	public int getSandstormBuildupTickRate() {
		return sandstormBuildupTickRate;
	}

	public int getSandstormMaxStackable() {
		return sandstormMaxStackable;
	}

	public int getSnowstormBuildupTickRate() {
		return snowstormBuildupTickRate;
	}

	public int getSnowstormMaxStackable() {
		return snowstormMaxStackable;
	}

	// TODO: This is not a good way to do things at all
	public boolean halveEventTime(GameProgressionState progression) {
		return progression.is(halveEventTime);
	}

	public static final class Timers {
		public static final Timers DEFAULT = new Timers(1200, 1200);

		public static final Codec<Timers> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					Codec.INT.fieldOf("min_time").forGetter(c -> c.minTime),
					Codec.INT.fieldOf("extra_rand_time").forGetter(c -> c.extraRandTime)
			).apply(instance, Timers::new);
		});

		public final int minTime;
		public final int extraRandTime;

		public Timers(int minTime, int extraRandTime) {
			this.minTime = minTime;
			this.extraRandTime = extraRandTime;
		}
	}
}
