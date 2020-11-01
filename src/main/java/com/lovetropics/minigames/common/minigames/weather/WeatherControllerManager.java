package com.lovetropics.minigames.common.minigames.weather;

import com.lovetropics.minigames.Constants;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class WeatherControllerManager {
	private static final Map<DimensionType, WeatherController> WEATHER_CONTROLLERS = new Reference2ObjectOpenHashMap<>();

	private static Function<ServerWorld, WeatherController> factory = VanillaWeatherController::new;

	public static void setFactory(Function<ServerWorld, WeatherController> factory) {
		WeatherControllerManager.factory = factory;
	}

	public static WeatherController forWorld(ServerWorld world) {
		DimensionType dimension = world.getDimension().getType();
		WeatherController controller = WEATHER_CONTROLLERS.get(dimension);
		if (controller == null) {
			WEATHER_CONTROLLERS.put(dimension, controller = factory.apply(world));
		}
		return controller;
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		World world = event.world;
		if (world.isRemote) {
			return;
		}

		WeatherController controller = WEATHER_CONTROLLERS.get(world.dimension.getType());
		if (controller != null) {
			controller.tick();
		}
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event) {
		WEATHER_CONTROLLERS.remove(event.getWorld().getDimension().getType());
	}
}
