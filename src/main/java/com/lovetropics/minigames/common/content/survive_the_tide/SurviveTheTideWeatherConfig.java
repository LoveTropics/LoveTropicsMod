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
				MoreCodecs.object2Float(Codec.STRING).fieldOf("heatwave_chance").forGetter(c -> c.phaseToHeatwaveChance),
				MoreCodecs.object2Float(Codec.STRING).fieldOf("wind_speed").forGetter(c -> c.phaseToWindSpeed),
				Codec.INT.optionalFieldOf("rain_heavy_min_time", 1200).forGetter(c -> c.rainHeavyMinTime),
				Codec.INT.optionalFieldOf("rain_heavy_extra_rand_time", 1200).forGetter(c -> c.rainHeavyExtraRandTime),
				Codec.INT.optionalFieldOf("rain_acid_min_time", 1200).forGetter(c -> c.rainAcidMinTime),
				Codec.INT.optionalFieldOf("rain_acid_extra_rand_time", 1200).forGetter(c -> c.rainAcidExtraRandTime),
				Codec.INT.optionalFieldOf("heatwave_min_time", 1200).forGetter(c -> c.heatwaveMinTime),
				Codec.INT.optionalFieldOf("heatwave_extra_rand_time", 1200).forGetter(c -> c.heatwaveExtraRandTime),
				Codec.DOUBLE.optionalFieldOf("heatwave_movement_multiplier", 0.5).forGetter(c -> c.heatwaveMovementMultiplier),
				Codec.FLOAT.optionalFieldOf("acid_rain_damage", 1.0F).forGetter(c -> c.acidRainDamage),
				Codec.DOUBLE.optionalFieldOf("acid_rain_damage_rate", 60.0).forGetter(c -> c.acidRainDamageRate)
		).apply(instance, SurviveTheTideWeatherConfig::new);
	});

	private final Object2FloatMap<String> phaseToHeavyRainChance;
	private final Object2FloatMap<String> phaseToAcidRainChance;
	private final Object2FloatMap<String> phaseToHeatwaveChance;
	private final Object2FloatMap<String> phaseToWindSpeed;

	private final int rainHeavyMinTime;
	private final int rainHeavyExtraRandTime;

	private final int rainAcidMinTime;
	private final int rainAcidExtraRandTime;

	private final int heatwaveMinTime;
	private final int heatwaveExtraRandTime;
	private final double heatwaveMovementMultiplier;

	private final float acidRainDamage;
	private final double acidRainDamageRate;

	public SurviveTheTideWeatherConfig(
			Object2FloatMap<String> phaseToHeavyRainChance, Object2FloatMap<String> phaseToAcidRainChance, Object2FloatMap<String> phaseToHeatwaveChance,
			Object2FloatMap<String> phaseToWindSpeed,
			final int rainHeavyMinTime, final int rainHeavyExtraRandTime,
			final int rainAcidMinTime, final int rainAcidExtraRandTime,
			int heatwaveMinTime, int heatwaveExtraRandTime, final double heatwaveMovementMultiplier,
			final float acidRainDamage, final double acidRainDamageRate
	) {
		this.phaseToHeavyRainChance = phaseToHeavyRainChance;
		this.phaseToAcidRainChance = phaseToAcidRainChance;
		this.phaseToHeatwaveChance = phaseToHeatwaveChance;
		this.phaseToWindSpeed = phaseToWindSpeed;

		this.rainHeavyMinTime = rainHeavyMinTime;
		this.rainHeavyExtraRandTime = rainHeavyExtraRandTime;

		this.rainAcidMinTime = rainAcidMinTime;
		this.rainAcidExtraRandTime = rainAcidExtraRandTime;
		this.heatwaveMinTime = heatwaveMinTime;
		this.heatwaveExtraRandTime = heatwaveExtraRandTime;

		this.heatwaveMovementMultiplier = heatwaveMovementMultiplier;

		this.acidRainDamage = acidRainDamage;
		this.acidRainDamageRate = acidRainDamageRate;
	}

	public double getRainHeavyChance(String phase) {
		return phaseToHeavyRainChance.getOrDefault(phase, 0.0F);
	}

	public double getRainAcidChance(String phase) {
		return phaseToAcidRainChance.getOrDefault(phase, 0.0F);
	}

	public double getHeatwaveChance(String phase) {
		return phaseToHeatwaveChance.getOrDefault(phase, 0.0F);
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

	public double getAcidRainDamageRate() {
		return acidRainDamageRate;
	}

	public float getAcidRainDamage() {
		return acidRainDamage;
	}

	public int getHeatwaveMinTime() {
		return heatwaveMinTime;
	}

	public int getHeatwaveExtraRandTime() {
		return heatwaveExtraRandTime;
	}

	public double getHeatwaveMovementMultiplier() {
		return heatwaveMovementMultiplier;
	}

	public int getRainAcidExtraRandTime() {
		return rainAcidExtraRandTime;
	}

	public int getRainAcidMinTime() {
		return rainAcidMinTime;
	}
}
