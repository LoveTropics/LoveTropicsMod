package com.lovetropics.minigames.common.minigames.weather;

import com.mojang.datafixers.Dynamic;

public class SurviveTheTideWeatherConfig {
	private final double rainHeavyChance;
	private final double rainAcidChance;
	private final double heatwaveChance;

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
			final double rainHeavyChance, final double rainAcidChance, final double heatwaveChance,
			final int rainHeavyMinTime, final int rainHeavyExtraRandTime,
			final int rainAcidMinTime, final int rainAcidExtraRandTime,
			int heatwaveMinTime, int heatwaveExtraRandTime, final double heatwaveMovementMultiplier,
			final float acidRainDamage, final double acidRainDamageRate
	) {
		this.rainHeavyChance = rainHeavyChance;
		this.rainAcidChance = rainAcidChance;
		this.heatwaveChance = heatwaveChance;

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

	public static <T> SurviveTheTideWeatherConfig parse(final Dynamic<T> root) {
		final double rainHeavyChance = root.get("rain_heavy_chance").asDouble(0.01);
		final double rainAcidChance = root.get("rain_acid_chance").asDouble(0.01);
		final double heatwaveChance = root.get("heatwave_chance").asDouble(0.01);

		final int rainHeavyMinTime = root.get("rain_heavy_min_time").asInt(1200);
		final int rainHeavyExtraRandTime = root.get("rain_heavy_extra_rand_time").asInt(1200);

		final int rainAcidMinTime = root.get("rain_acid_min_time").asInt(1200);
		final int rainAcidExtraRandTime = root.get("rain_acid_extra_rand_time").asInt(1200);

		final int heatwaveMinTime = root.get("heatwave_min_time").asInt(1200);
		final int heatwaveExtraRandTime = root.get("heatwave_extra_rand_time").asInt(1200);

		final double heatwaveMovementMultiplier = root.get("heatwave_movement_multiplier").asDouble(0.5);

		final float acidRainDamage = root.get("acid_rain_damage").asFloat(1);
		final double acidRainDamageRate = root.get("acid_rain_damage_rate").asDouble(60);

		return new SurviveTheTideWeatherConfig(rainHeavyChance, rainAcidChance, heatwaveChance, rainHeavyMinTime,
				rainHeavyExtraRandTime, rainAcidMinTime, rainAcidExtraRandTime, heatwaveMinTime, heatwaveExtraRandTime, heatwaveMovementMultiplier, acidRainDamage, acidRainDamageRate);
	}

	public double getRainHeavyChance() {
		return rainHeavyChance;
	}

	public double getRainAcidChance() {
		return rainAcidChance;
	}

	public double getHeatwaveChance() {
		return heatwaveChance;
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
