package com.lovetropics.minigames.common.core.game.weather;

import net.minecraft.network.PacketBuffer;

public final class WeatherState {
	public float rainAmount;
	public RainType rainType = RainType.NORMAL;
	public float windSpeed;
	public boolean heatwave;
	public boolean sandstorm;
	public boolean snowstorm;

	public void serialize(PacketBuffer buffer) {
		buffer.writeFloat(this.rainAmount);
		buffer.writeByte(this.rainType.ordinal() & 0xFF);
		buffer.writeFloat(this.windSpeed);
		buffer.writeBoolean(this.heatwave);
		buffer.writeBoolean(this.sandstorm);
		buffer.writeBoolean(this.snowstorm);
	}

	public void deserialize(PacketBuffer buffer) {
		this.rainAmount = buffer.readFloat();
		this.rainType = RainType.VALUES[buffer.readUnsignedByte()];
		this.windSpeed = buffer.readFloat();
		this.heatwave = buffer.readBoolean();
		this.sandstorm = buffer.readBoolean();
		this.snowstorm = buffer.readBoolean();
	}

	public boolean isRaining() {
		return this.rainAmount > 0.01F;
	}

	public boolean isWindy() {
		return this.windSpeed > 0.01F;
	}

	public boolean hasWeather() {
		return (this.isRaining() && this.isWindy()) || this.heatwave || this.sandstorm || this.snowstorm;
	}
}
