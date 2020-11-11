package com.lovetropics.minigames.common.map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.GameRules;
import net.minecraft.world.storage.WorldInfo;

public final class MapWorldSettings {
	public final GameRules gameRules = new GameRules();
	public long timeOfDay;

	public int sunnyTime;
	public boolean raining;
	public int rainTime;
	public boolean thundering;
	public int thunderTime;

	public static MapWorldSettings createFrom(WorldInfo info) {
		MapWorldSettings settings = new MapWorldSettings();
		settings.gameRules.read(info.getGameRulesInstance().write());
		settings.timeOfDay = info.getDayTime();
		settings.sunnyTime = info.getClearWeatherTime();
		settings.raining = info.isRaining();
		settings.rainTime = info.getRainTime();
		settings.thundering = info.isThundering();
		settings.thunderTime = info.getThunderTime();

		return settings;
	}

	public CompoundNBT write(CompoundNBT root) {
		root.putLong("time_of_day", this.timeOfDay);
		root.put("game_rules", this.gameRules.write());

		root.putInt("sunny_time", this.sunnyTime);
		root.putBoolean("raining", this.raining);
		root.putInt("rain_time", this.rainTime);
		root.putBoolean("thundering", this.thundering);
		root.putInt("thunder_time", this.thunderTime);

		return root;
	}

	public void read(CompoundNBT root) {
		this.timeOfDay = root.getLong("time_of_day");
		this.gameRules.read(root.getCompound("game_rules"));

		this.sunnyTime = root.getInt("sunny_time");
		this.raining = root.getBoolean("raining");
		this.rainTime = root.getInt("rain_time");
		this.thundering = root.getBoolean("thundering");
		this.thunderTime = root.getInt("thunder_time");
	}

	public void importFrom(MapWorldSettings settings) {
		read(settings.write(new CompoundNBT()));
	}
}
