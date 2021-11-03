package com.lovetropics.minigames.common.core.game.weather;

import net.minecraft.network.PacketBuffer;

public final class WeatherState {
	public float rainAmount;
	public RainType rainType = RainType.NORMAL;
	public float windSpeed;
	public boolean heatwave;
	public StormState sandstorm;
	public StormState snowstorm;

	public void serialize(PacketBuffer buffer) {
		buffer.writeFloat(this.rainAmount);
		buffer.writeByte(this.rainType.ordinal() & 0xFF);
		buffer.writeFloat(this.windSpeed);
		buffer.writeBoolean(this.heatwave);
		buffer.writeBoolean(this.sandstorm != null);
		if (this.sandstorm != null) {
			this.snowstorm.encode(buffer);
		}
		buffer.writeBoolean(this.snowstorm != null);
		if (this.snowstorm != null) {
			this.snowstorm.encode(buffer);
		}
	}

	public void deserialize(PacketBuffer buffer) {
		this.rainAmount = buffer.readFloat();
		this.rainType = RainType.VALUES[buffer.readUnsignedByte()];
		this.windSpeed = buffer.readFloat();
		this.heatwave = buffer.readBoolean();
		this.sandstorm = buffer.readBoolean() ? StormState.decode(buffer) : null;
		this.snowstorm = buffer.readBoolean() ? StormState.decode(buffer) : null;
	}

	public boolean isRaining() {
		return this.rainAmount > 0.01F;
	}

	public boolean isWindy() {
		return this.windSpeed > 0.01F;
	}

	public boolean hasWeather() {
		return (this.isRaining() && this.isWindy())
				|| this.heatwave
				|| this.sandstorm != null || this.snowstorm != null;
	}
}
