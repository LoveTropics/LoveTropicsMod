package com.lovetropics.minigames.common.core.map;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.storage.DerivedWorldInfo;
import net.minecraft.world.storage.IServerConfiguration;
import net.minecraft.world.storage.IServerWorldInfo;

public final class MapWorldInfo extends DerivedWorldInfo {
	private final MapWorldSettings settings;

	public MapWorldInfo(IServerConfiguration serverConfiguration, IServerWorldInfo overworld) {
		super(serverConfiguration, overworld);
		this.settings = MapWorldSettings.createFrom(overworld);
	}

	public MapWorldInfo(IServerConfiguration serverConfiguration, IServerWorldInfo overworld, MapWorldSettings settings) {
		super(serverConfiguration, overworld);
		this.settings = settings;
	}

	public static MapWorldInfo create(MinecraftServer server, MapWorldSettings settings) {
		return new MapWorldInfo(server.getServerConfiguration(), (IServerWorldInfo) server.func_241755_D_().getWorldInfo(), settings);
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
