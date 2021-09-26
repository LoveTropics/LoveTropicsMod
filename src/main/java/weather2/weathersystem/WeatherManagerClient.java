package weather2.weathersystem;

import net.minecraft.client.Minecraft;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class WeatherManagerClient extends WeatherManager {
	public WeatherManagerClient(RegistryKey<World> dimension) {
		super(dimension);
	}
	
	@Override
	public World getWorld() {
		return Minecraft.getInstance().world;
	}
}
