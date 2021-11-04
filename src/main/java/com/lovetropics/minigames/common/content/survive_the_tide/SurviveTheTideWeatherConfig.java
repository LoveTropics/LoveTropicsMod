package com.lovetropics.minigames.common.content.survive_the_tide;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;

public class SurviveTheTideWeatherConfig {
	public static final Codec<SurviveTheTideWeatherConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				MoreCodecs.object2Float(Codec.STRING).fieldOf("rain_heavy_chance").forGetter(c -> c.phaseToHeavyRainChance),
				MoreCodecs.object2Float(Codec.STRING).fieldOf("rain_acid_chance").forGetter(c -> c.phaseToAcidRainChance),
				MoreCodecs.object2Float(Codec.STRING).fieldOf("hail_chance").forGetter(c -> c.phaseToHailChance),
				MoreCodecs.object2Float(Codec.STRING).fieldOf("heatwave_chance").forGetter(c -> c.phaseToHeatwaveChance),
				MoreCodecs.object2Float(Codec.STRING).fieldOf("sandstorm_chance").forGetter(c -> c.phaseToSandstormChance),
				MoreCodecs.object2Float(Codec.STRING).fieldOf("snowstorm_chance").forGetter(c -> c.phaseToSnowstormChance),
				MoreCodecs.object2Float(Codec.STRING).fieldOf("wind_speed").forGetter(c -> c.phaseToWindSpeed),
				Codec.INT.optionalFieldOf("rain_heavy_min_time", 1200).forGetter(c -> c.rainHeavyMinTime),
				Codec.INT.optionalFieldOf("rain_heavy_extra_rand_time", 1200).forGetter(c -> c.rainHeavyExtraRandTime),
				Codec.INT.optionalFieldOf("rain_acid_min_time", 1200).forGetter(c -> c.rainAcidMinTime),
				Codec.INT.optionalFieldOf("rain_acid_extra_rand_time", 1200).forGetter(c -> c.rainAcidExtraRandTime),
				Codec.INT.optionalFieldOf("heatwave_min_time", 1200).forGetter(c -> c.heatwaveMinTime),
				Codec.INT.optionalFieldOf("heatwave_extra_rand_time", 1200).forGetter(c -> c.heatwaveExtraRandTime),
				Codec.INT.optionalFieldOf("sandstorm_buildup_tickrate", 40).forGetter(c -> c.sandstormBuildupTickRate),
				Codec.INT.optionalFieldOf("sandstorm_max_stackable", 1).forGetter(c -> c.sandstormMaxStackable),
				Codec.INT.optionalFieldOf("snowstorm_buildup_tickrate", 40).forGetter(c -> c.snowstormBuildupTickRate)
		).apply(instance, SurviveTheTideWeatherConfig::new);
	});

	private final Object2FloatMap<String> phaseToHeavyRainChance;
	private final Object2FloatMap<String> phaseToAcidRainChance;
	private final Object2FloatMap<String> phaseToHailChance;
	private final Object2FloatMap<String> phaseToHeatwaveChance;
	private final Object2FloatMap<String> phaseToSandstormChance;
	private final Object2FloatMap<String> phaseToSnowstormChance;
	private final Object2FloatMap<String> phaseToWindSpeed;

	private final int rainHeavyMinTime;
	private final int rainHeavyExtraRandTime;

	private final int rainAcidMinTime;
	private final int rainAcidExtraRandTime;

	private final int heatwaveMinTime;
	private final int heatwaveExtraRandTime;

	private final int sandstormBuildupTickRate;
	private final int sandstormMaxStackable;

	private final int snowstormBuildupTickRate;
	//private final int snowstormMaxStackable;

	public SurviveTheTideWeatherConfig(
			Object2FloatMap<String> phaseToHeavyRainChance, Object2FloatMap<String> phaseToAcidRainChance, Object2FloatMap<String> phaseToHailChance,
			Object2FloatMap<String> phaseToHeatwaveChance,
			Object2FloatMap<String> phaseToSandstormChance, Object2FloatMap<String> phaseToSnowstormChance,
			Object2FloatMap<String> phaseToWindSpeed,
			final int rainHeavyMinTime, final int rainHeavyExtraRandTime,
			final int rainAcidMinTime, final int rainAcidExtraRandTime,
			int heatwaveMinTime, int heatwaveExtraRandTime,
			final int sandstormBuildupTickRate, final int sandstormMaxStackable,
			final int snowstormBuildupTickRate
	) {
		this.phaseToHeavyRainChance = phaseToHeavyRainChance;
		this.phaseToAcidRainChance = phaseToAcidRainChance;
		this.phaseToHailChance = phaseToHailChance;
		this.phaseToHeatwaveChance = phaseToHeatwaveChance;
		this.phaseToSandstormChance = phaseToSandstormChance;
		this.phaseToSnowstormChance = phaseToSnowstormChance;
		this.phaseToWindSpeed = phaseToWindSpeed;

		this.rainHeavyMinTime = rainHeavyMinTime;
		this.rainHeavyExtraRandTime = rainHeavyExtraRandTime;

		this.rainAcidMinTime = rainAcidMinTime;
		this.rainAcidExtraRandTime = rainAcidExtraRandTime;
		this.heatwaveMinTime = heatwaveMinTime;
		this.heatwaveExtraRandTime = heatwaveExtraRandTime;

		this.sandstormBuildupTickRate = sandstormBuildupTickRate;
		this.sandstormMaxStackable = sandstormMaxStackable;

		this.snowstormBuildupTickRate = snowstormBuildupTickRate;
		//this.snowstormMaxStackable = snowstormMaxStackable;
	}

	public double getRainHeavyChance(String phase) {
		return phaseToHeavyRainChance.getOrDefault(phase, 0.0F);
	}

	public double getRainAcidChance(String phase) {
		return phaseToAcidRainChance.getOrDefault(phase, 0.0F);
	}

	public double getHailChance(String phase) {
		return phaseToHailChance.getOrDefault(phase, 0.0F);
	}

	public double getHeatwaveChance(String phase) {
		return phaseToHeatwaveChance.getOrDefault(phase, 0.0F);
	}

	public double getSandstormChance(String phase) {
		return phaseToSandstormChance.getOrDefault(phase, 0.0F);
	}

	public double getSnowstormChance(String phase) {
		return phaseToSnowstormChance.getOrDefault(phase, 0.0F);
	}

	public float getWindSpeed(String phase) {
		return phaseToWindSpeed.getOrDefault(phase, 0.0F);
	}

	public int getRainHeavyMinTime() {
		return rainHeavyMinTime;
	}

	public int getRainHeavyExtraRandTime() {
		return rainHeavyExtraRandTime;
	}

	public int getHeatwaveMinTime() {
		return heatwaveMinTime;
	}

	public int getHeatwaveExtraRandTime() {
		return heatwaveExtraRandTime;
	}

	public int getRainAcidExtraRandTime() {
		return rainAcidExtraRandTime;
	}

	public int getRainAcidMinTime() {
		return rainAcidMinTime;
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
		return sandstormMaxStackable;
	}
}
