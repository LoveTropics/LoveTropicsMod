package com.lovetropics.minigames.common.core.game.weather;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;

public final class VanillaWeatherController implements WeatherController {
	private final ServerWorld world;
	private final WeatherState state = new WeatherState();

	public VanillaWeatherController(ServerWorld world) {
		this.world = world;
	}

	@Override
	public void onPlayerJoin(ServerPlayerEntity player) {
	}

	@Override
	public void tick() {
		world.getWorldInfo().setRaining(state.isRaining());
	}

	@Override
	public void setRain(float amount, RainType type) {
		state.rainAmount = amount;
		state.rainType = type;

		boolean raining = amount > 0.0F;
		IServerWorldInfo info = (IServerWorldInfo) world.getWorldInfo();
		info.setRaining(raining);
		info.setThundering(raining);
	}

	@Override
	public void setWind(float speed) {
		state.windSpeed = speed;
	}

	@Override
	public void setHeatwave(boolean heatwave) {
		state.heatwave = heatwave;
	}

	@Override
	public void setSandstorm(boolean sandstorm) {
		state.sandstorm = sandstorm;
	}

	@Override
	public void setSnowstorm(boolean snowstorm) {
		state.snowstorm = snowstorm;
	}

	@Override
	public float getRainAmount() {
		return state.rainAmount;
	}

	@Override
	public RainType getRainType() {
		return state.rainType;
	}

	@Override
	public float getWindSpeed() {
		return state.windSpeed;
	}

	@Override
	public boolean isHeatwave() {
		return state.heatwave;
	}

	@Override
	public boolean isSandstorm() {
		return state.sandstorm;
	}

	@Override
	public boolean isSnowstorm() {
		return state.snowstorm;
	}
}
