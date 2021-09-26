package weather2.weathersystem;

import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class WeatherManagerServer extends WeatherManager {
	private final ServerWorld world;

	public WeatherManagerServer(ServerWorld world) {
		super(world.getDimensionKey());
		this.world = world;
	}

	@Override
	public World getWorld() {
		return world;
	}
}
