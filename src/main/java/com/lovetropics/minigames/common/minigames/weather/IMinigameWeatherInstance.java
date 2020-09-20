package com.lovetropics.minigames.common.minigames.weather;

import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IMinigameWeatherInstance<T extends IMinigameDefinition> extends INBTSerializable<CompoundNBT> {

	void tick(T minigameDefinition);

	void tickPlayer(PlayerEntity player);

	void reset();

	boolean heavyRainfallActive();

	boolean acidRainActive();

	boolean heatwaveActive();

	boolean specialWeatherActive();

	boolean isLastRainWasAcid();

	void setLastRainWasAcid(boolean lastRainWasAcid);

	boolean isMinigameActive();

	void setMinigameActive(boolean minigameActive);

	float getWindSpeed();
	
	class Noop<T extends IMinigameDefinition> implements IMinigameWeatherInstance<T> {

		@Override
		public CompoundNBT serializeNBT() {
			return new CompoundNBT();
		}

		@Override
		public void deserializeNBT(CompoundNBT nbt) {}

		@Override
		public void tick(T minigameDefinition) {}

		@Override
		public void tickPlayer(PlayerEntity player) {}

		@Override
		public void reset() {}

		@Override
		public boolean heavyRainfallActive() {
			return false;
		}

		@Override
		public boolean acidRainActive() {
			return false;
		}

		@Override
		public boolean heatwaveActive() {
			return false;
		}

		@Override
		public boolean specialWeatherActive() {
			return false;
		}

		@Override
		public boolean isLastRainWasAcid() {
			return false;
		}

		@Override
		public void setLastRainWasAcid(boolean lastRainWasAcid) {}

		@Override
		public boolean isMinigameActive() {
			return true;
		}

		@Override
		public void setMinigameActive(boolean minigameActive) {}

		@Override
		public float getWindSpeed() {
			return 0;
		}
	}

}