package weather2.weathersystem;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import weather2.weathersystem.wind.WindManager;

public abstract class WeatherManager {
	public final RegistryKey<World> dimension;
	public final WindManager wind = new WindManager(this);

	public WeatherManager(RegistryKey<World> dimension) {
		this.dimension = dimension;
	}

	public abstract World getWorld();

	public void tick() {
		wind.tick(getWorld());
	}

	public WindManager getWindManager() {
		return this.wind;
	}
}
