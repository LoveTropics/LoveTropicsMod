package weather2;

import com.lovetropics.minigames.common.core.game.weather.RainType;
import com.lovetropics.minigames.common.core.game.weather.WeatherState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Weather.MODID, value = Dist.CLIENT)
public final class ClientWeather {
	private static ClientWeather instance = new ClientWeather();

	private static final int LERP_TICKS = ServerWeatherController.UPDATE_INTERVAL;

	private final WeatherState state = new WeatherState();

	private WeatherState lerpState = this.state;
	private int lerpTicks;

	private ClientWeather() {
	}

	public static ClientWeather get() {
		return instance;
	}

	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event) {
		if (event.getWorld().isRemote()) {
			reset();
		}
	}

	public static void reset() {
		instance = new ClientWeather();
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			instance.tick();
		}
	}

	public void onUpdateWeather(WeatherState state) {
		this.lerpTicks = LERP_TICKS;
		this.lerpState = state;
	}

	public void tick() {
		if (this.lerpTicks <= 0) {
			this.state.rainType = this.lerpState.rainType;
			this.state.heatwave = this.lerpState.heatwave;
			return;
		}

		float lerpTicks = this.lerpTicks--;
		this.state.rainAmount = this.state.rainAmount + (this.lerpState.rainAmount - this.state.rainAmount) / lerpTicks;
		this.state.windSpeed = this.state.windSpeed + (this.lerpState.windSpeed - this.state.windSpeed) / lerpTicks;
	}

	public float getRainAmount() {
		return this.state.rainAmount;
	}

	public float getVanillaRainAmount() {
		//have vanilla rain get to max saturation by the time rainAmount hits 0.33, rest of range is used for extra particle effects elsewhere
		return Math.min(this.state.rainAmount * 3F, 1F);
	}

	public RainType getRainType() {
		return this.state.rainType;
	}

	public float getWindSpeed() {
		return this.state.windSpeed;
	}

	public boolean isHeatwave() {
		return this.state.heatwave;
	}

	public boolean hasWeather() {
		if (true) return true;
		return this.state.hasWeather();
	}
}
