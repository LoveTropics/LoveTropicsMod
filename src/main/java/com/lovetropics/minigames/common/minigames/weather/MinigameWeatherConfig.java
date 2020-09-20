package com.lovetropics.minigames.common.minigames.weather;

import com.google.gson.JsonElement;
import com.mojang.datafixers.Dynamic;

public class MinigameWeatherConfig
{
	private final double rainHeavyChance;
	private final double rainAcidChance;
	private final double heatwaveChance;

	private final int rainHeavyMinTime;
	private final int rainHeavyExtraRandTime;

	private final int rainAcidMinTime;
	private final int rainAcidExtraRandTime;

	private final double heatwaveMovementMultiplier;

	private final double acidRainDamage;
	private final double acidRainDamageRate;

	public MinigameWeatherConfig(final double rainHeavyChance, final double rainAcidChance, final double heatwaveChance,
			final int rainHeavyMinTime, final int rainHeavyExtraRandTime, final int rainAcidMinTime, final int rainAcidExtraRandTime,
			final double heatwaveMovementMultiplier, final double acidRainDamage, final double acidRainDamageRate) {
		this.rainHeavyChance = rainHeavyChance;
		this.rainAcidChance = rainAcidChance;
		this.heatwaveChance = heatwaveChance;

		this.rainHeavyMinTime = rainHeavyMinTime;
		this.rainHeavyExtraRandTime = rainHeavyExtraRandTime;

		this.rainAcidMinTime = rainAcidMinTime;
		this.rainAcidExtraRandTime = rainAcidExtraRandTime;

		this.heatwaveMovementMultiplier = heatwaveMovementMultiplier;

		this.acidRainDamage = acidRainDamage;
		this.acidRainDamageRate = acidRainDamageRate;
	}

	public static MinigameWeatherConfig deserialize(final Dynamic<JsonElement> root) {
		final double rainHeavyChance = root.get("rain_heavy_chance").asDouble(0.01);
		final double rainAcidChance = root.get("rain_acid_chance").asDouble(0.01);
		final double heatwaveChance = root.get("heatwave_chance").asDouble(0.01);

		final int rainHeavyMinTime = root.get("rain_heavy_min_time").asInt(1200);
		final int rainHeavyExtraRandTime = root.get("rain_heavy_extra_rand_time").asInt(1200);

		final int rainAcidMinTime = root.get("rain_acid_min_time").asInt(1200);
		final int rainAcidExtraRandTime = root.get("rain_acid_extra_rand_time").asInt(1200);

		final double heatwaveMovementMultiplier = root.get("heatwave_movement_multiplier").asDouble(0.5);

		final double acidRainDamage = root.get("acid_rain_damage").asInt(1);
		final double acidRainDamageRate = root.get("acid_rain_damage_rate").asInt(60);

		return new MinigameWeatherConfig(rainHeavyChance, rainAcidChance, heatwaveChance, rainHeavyMinTime,
				rainHeavyExtraRandTime, rainAcidMinTime, rainAcidExtraRandTime, heatwaveMovementMultiplier, acidRainDamage, acidRainDamageRate);
	}

	public double getRainHeavyChance()
	{
		return rainHeavyChance;
	}

	public double getRainAcidChance()
	{
		return rainAcidChance;
	}

	public double getHeatwaveChance()
	{
		return heatwaveChance;
	}

	public int getRainHeavyMinTime()
	{
		return rainHeavyMinTime;
	}

	public int getRainHeavyExtraRandTime()
	{
		return rainHeavyExtraRandTime;
	}

	public double getAcidRainDamageRate()
	{
		return acidRainDamageRate;
	}

	public double getAcidRainDamage()
	{
		return acidRainDamage;
	}

	public double getHeatwaveMovementMultiplier()
	{
		return heatwaveMovementMultiplier;
	}

	public int getRainAcidExtraRandTime()
	{
		return rainAcidExtraRandTime;
	}

	public int getRainAcidMinTime()
	{
		return rainAcidMinTime;
	}
}
