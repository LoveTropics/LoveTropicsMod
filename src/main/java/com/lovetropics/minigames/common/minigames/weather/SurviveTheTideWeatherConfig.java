package com.lovetropics.minigames.common.minigames.weather;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.DynamicLike;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

public class SurviveTheTideWeatherConfig {
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

	public static <T> SurviveTheTideWeatherConfig parse(final Dynamic<T> root) {
		final Object2FloatMap<String> rainHeavyChance = parsePhaseToFloatMap(root.get("rain_heavy_chance"));
		final Object2FloatMap<String> rainAcidChance = parsePhaseToFloatMap(root.get("rain_acid_chance"));
		final Object2FloatMap<String> heatwaveChance = parsePhaseToFloatMap(root.get("heatwave_chance"));
		final Object2FloatMap<String> windSpeed = parsePhaseToFloatMap(root.get("wind_speed"));

		final int rainHeavyMinTime = root.get("rain_heavy_min_time").asInt(1200);
		final int rainHeavyExtraRandTime = root.get("rain_heavy_extra_rand_time").asInt(1200);

		final int rainAcidMinTime = root.get("rain_acid_min_time").asInt(1200);
		final int rainAcidExtraRandTime = root.get("rain_acid_extra_rand_time").asInt(1200);

		final int heatwaveMinTime = root.get("heatwave_min_time").asInt(1200);
		final int heatwaveExtraRandTime = root.get("heatwave_extra_rand_time").asInt(1200);

		final double heatwaveMovementMultiplier = root.get("heatwave_movement_multiplier").asDouble(0.5);

		final float acidRainDamage = root.get("acid_rain_damage").asFloat(1);
		final double acidRainDamageRate = root.get("acid_rain_damage_rate").asDouble(60);

		return new SurviveTheTideWeatherConfig(rainHeavyChance, rainAcidChance, heatwaveChance, windSpeed, rainHeavyMinTime,
				rainHeavyExtraRandTime, rainAcidMinTime, rainAcidExtraRandTime, heatwaveMinTime, heatwaveExtraRandTime, heatwaveMovementMultiplier, acidRainDamage, acidRainDamageRate);
	}

	private static <T> Object2FloatMap<String> parsePhaseToFloatMap(DynamicLike<T> root) {
		return new Object2FloatOpenHashMap<>(root.asMap(
				key -> key.asString(""),
				value -> value.asFloat(0.0F)
		));
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
