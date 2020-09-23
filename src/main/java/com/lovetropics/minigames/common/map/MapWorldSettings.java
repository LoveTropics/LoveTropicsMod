package com.lovetropics.minigames.common.map;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.GameRules;
import net.minecraft.world.storage.WorldInfo;

public final class MapWorldSettings {
	public final GameRules gameRules = new GameRules();
	public long timeOfDay;

	public static MapWorldSettings createFrom(WorldInfo info) {
		MapWorldSettings settings = new MapWorldSettings();
		settings.gameRules.read(info.getGameRulesInstance().write());
		settings.timeOfDay = info.getDayTime();

		return settings;
	}

	public CompoundNBT write(CompoundNBT root) {
		root.putLong("time_of_day", this.timeOfDay);
		root.put("game_rules", this.gameRules.write());

		return root;
	}

	public void read(CompoundNBT root) {
		this.timeOfDay = root.getLong("time_of_day");
		this.gameRules.read(root.getCompound("game_rules"));
	}
}
