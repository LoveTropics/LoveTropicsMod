package com.lovetropics.minigames.common.core.map;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.world.storage.IServerWorldInfo;

public final class MapWorldSettings {
	public static final Codec<MapWorldSettings> CODEC = MoreCodecs.withNbtCompound(MapWorldSettings::write, MapWorldSettings::read, MapWorldSettings::new);

	public final GameRules gameRules = new GameRules();
	public long timeOfDay;

	public int sunnyTime;
	public boolean raining;
	public int rainTime;
	public boolean thundering;
	public int thunderTime;
	public Difficulty difficulty = Difficulty.NORMAL;

	public static MapWorldSettings createFromOverworld(MinecraftServer server) {
		return createFrom((IServerWorldInfo) server.overworld().getLevelData());
	}

	public static MapWorldSettings createFrom(IServerWorldInfo info) {
		MapWorldSettings settings = new MapWorldSettings();
		settings.gameRules.loadFromTag(new Dynamic<>(NBTDynamicOps.INSTANCE, info.getGameRules().createTag()));
		settings.timeOfDay = info.getDayTime();
		settings.sunnyTime = info.getClearWeatherTime();
		settings.raining = info.isRaining();
		settings.rainTime = info.getRainTime();
		settings.thundering = info.isThundering();
		settings.thunderTime = info.getThunderTime();
		settings.difficulty = info.getDifficulty();

		return settings;
	}

	public CompoundNBT write(CompoundNBT root) {
		root.putLong("time_of_day", this.timeOfDay);
		root.put("game_rules", this.gameRules.createTag());

		root.putInt("sunny_time", this.sunnyTime);
		root.putBoolean("raining", this.raining);
		root.putInt("rain_time", this.rainTime);
		root.putBoolean("thundering", this.thundering);
		root.putInt("thunder_time", this.thunderTime);

		root.putString("difficulty", this.difficulty.getKey());

		return root;
	}

	public void read(CompoundNBT root) {
		this.timeOfDay = root.getLong("time_of_day");
		this.gameRules.loadFromTag(new Dynamic<>(NBTDynamicOps.INSTANCE, root.getCompound("game_rules")));

		this.sunnyTime = root.getInt("sunny_time");
		this.raining = root.getBoolean("raining");
		this.rainTime = root.getInt("rain_time");
		this.thundering = root.getBoolean("thundering");
		this.thunderTime = root.getInt("thunder_time");

		Difficulty difficulty = Difficulty.byName(root.getString("difficulty"));
		this.difficulty = difficulty != null ? difficulty : Difficulty.NORMAL;
	}

	public void importFrom(MapWorldSettings settings) {
		read(settings.write(new CompoundNBT()));
	}
}
