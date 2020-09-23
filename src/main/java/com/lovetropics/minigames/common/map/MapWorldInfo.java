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
	public boolean isRaining() {
		return false;
	}

	@Override
	public boolean isThundering() {
		return false;
	}

	@Override
	public GameRules getGameRulesInstance() {
		return this.settings.gameRules;
	}
}
