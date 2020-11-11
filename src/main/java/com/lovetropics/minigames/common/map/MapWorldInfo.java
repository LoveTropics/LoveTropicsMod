package com.lovetropics.minigames.common.map;

import net.minecraft.world.GameRules;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.WorldInfo;

public final class MapWorldInfo extends DerivedWorldInfo {
	private final MapWorldSettings settings;

	public MapWorldInfo(WorldInfo overworld) {
		super(overworld);
		this.settings = MapWorldSettings.createFrom(overworld);
	}

	public MapWorldInfo(WorldInfo overworld, MapWorldSettings settings) {
		super(overworld);
		this.settings = settings;
	}

	@Override
	public void setDayTime(long time) {
		this.settings.timeOfDay = time;
	}

	@Override
	public long getDayTime() {
		return this.settings.timeOfDay;
	}

	@Override
	public void setClearWeatherTime(int time) {
		this.settings.sunnyTime = time;
	}

	@Override
	public void setRaining(boolean raining) {
		this.settings.raining = raining;
	}

	@Override
	public void setRainTime(int time) {
		this.settings.rainTime = time;
	}

	@Override
	public void setThundering(boolean thundering) {
		this.settings.thundering = thundering;
	}

	@Override
	public void setThunderTime(int time) {
		this.settings.thunderTime = time;
	}

	@Override
	public boolean isRaining() {
		return this.settings.raining;
	}

	@Override
	public int getRainTime() {
		return this.settings.rainTime;
	}

	@Override
	public boolean isThundering() {
		return this.settings.thundering;
	}

	@Override
	public int getThunderTime() {
		return this.settings.thunderTime;
	}

	@Override
	public int getClearWeatherTime() {
		return this.settings.sunnyTime;
	}

	@Override
	public GameRules getGameRulesInstance() {
		return this.settings.gameRules;
	}
}
