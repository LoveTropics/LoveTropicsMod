package com.lovetropics.minigames.common.content.survive_the_tide;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.weather.WeatherEventType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;

import java.util.Map;

public class SurviveTheTideWeatherConfig {
	public static final Codec<SurviveTheTideWeatherConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.unboundedMap(WeatherEventType.CODEC, MoreCodecs.object2Float(Codec.STRING)).fieldOf("event_chances").forGetter(c -> c.eventChancesByPhase),
				Codec.unboundedMap(WeatherEventType.CODEC, Timers.CODEC).fieldOf("event_timers").forGetter(c -> c.eventTimers),
				MoreCodecs.object2Float(Codec.STRING).fieldOf("wind_speed").forGetter(c -> c.phaseToWindSpeed),
				Codec.INT.optionalFieldOf("sandstorm_buildup_tickrate", 40).forGetter(c -> c.sandstormBuildupTickRate),
				Codec.INT.optionalFieldOf("sandstorm_max_stackable", 1).forGetter(c -> c.sandstormMaxStackable),
				Codec.INT.optionalFieldOf("snowstorm_buildup_tickrate", 40).forGetter(c -> c.snowstormBuildupTickRate),
				Codec.INT.optionalFieldOf("snowstorm_max_stackable", 1).forGetter(c -> c.snowstormMaxStackable)
		).apply(instance, SurviveTheTideWeatherConfig::new);
	});

	private final Map<WeatherEventType, Object2FloatMap<String>> eventChancesByPhase;
	private final Map<WeatherEventType, Timers> eventTimers;

	private final Object2FloatMap<String> phaseToWindSpeed;

	private final int sandstormBuildupTickRate;
	private final int sandstormMaxStackable;

	private final int snowstormBuildupTickRate;
	private final int snowstormMaxStackable;

	public SurviveTheTideWeatherConfig(
			Map<WeatherEventType, Object2FloatMap<String>> eventChancesByPhase,
			Map<WeatherEventType, Timers> eventTimers,
			Object2FloatMap<String> phaseToWindSpeed,
			final int sandstormBuildupTickRate, final int sandstormMaxStackable,
			final int snowstormBuildupTickRate, final int snowstormMaxStackable
	) {
		this.eventChancesByPhase = eventChancesByPhase;
		this.eventTimers = eventTimers;
		this.phaseToWindSpeed = phaseToWindSpeed;

		this.sandstormBuildupTickRate = sandstormBuildupTickRate;
		this.sandstormMaxStackable = sandstormMaxStackable;

		this.snowstormBuildupTickRate = snowstormBuildupTickRate;
		this.snowstormMaxStackable = snowstormMaxStackable;
	}

	private float getEventChance(WeatherEventType event, String phase) {
		Object2FloatMap<String> chances = this.eventChancesByPhase.get(event);
		if (chances != null) {
			return chances.getOrDefault(phase, 0.0F);
		} else {
			return 0.0F;
		}
	}

	private Timers getTimers(WeatherEventType event) {
		return eventTimers.getOrDefault(event, Timers.DEFAULT);
	}

	public double getRainHeavyChance(String phase) {
		return getEventChance(WeatherEventType.HEATWAVE, phase);
	}

	public double getRainAcidChance(String phase) {
		return getEventChance(WeatherEventType.ACID_RAIN, phase);
	}

	public double getHailChance(String phase) {
		return getEventChance(WeatherEventType.HAIL, phase);
	}

	public double getHeatwaveChance(String phase) {
		return getEventChance(WeatherEventType.HEATWAVE, phase);
	}

	public double getSandstormChance(String phase) {
		return getEventChance(WeatherEventType.SANDSTORM, phase);
	}

	public double getSnowstormChance(String phase) {
		return getEventChance(WeatherEventType.SNOWSTORM, phase);
	}

	public float getWindSpeed(String phase) {
		return phaseToWindSpeed.getOrDefault(phase, 0.0F);
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
