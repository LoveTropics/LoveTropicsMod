package com.lovetropics.minigames.common.core.game.weather;

import com.lovetropics.minigames.Constants;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class WeatherControllerManager {
	private static final Map<RegistryKey<World>, WeatherController> WEATHER_CONTROLLERS = new Reference2ObjectOpenHashMap<>();

	private static Function<ServerWorld, WeatherController> factory = VanillaWeatherController::new;

	public static void setFactory(Function<ServerWorld, WeatherController> factory) {
		WeatherControllerManager.factory = factory;
	}

	public static WeatherController forWorld(ServerWorld world) {
		RegistryKey<World> dimension = world.getDimensionKey();
		WeatherController controller = WEATHER_CONTROLLERS.get(dimension);
		if (controller == null) {
			WEATHER_CONTROLLERS.put(dimension, controller = factory.apply(world));
		}
		return controller;
	}

	@SubscribeEvent
	public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
		joinPlayerToDimension(event.getPlayer(), event.getPlayer().world.getDimensionKey());
	}

	@SubscribeEvent
	public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
		joinPlayerToDimension(event.getPlayer(), event.getTo());
	}

	private static void joinPlayerToDimension(PlayerEntity player, RegistryKey<World> dimension) {
		MinecraftServer server = player.getServer();
		if (server == null || !(player instanceof ServerPlayerEntity)) {
			return;
		}

		ServerWorld world = server.getWorld(dimension);
		if (world != null) {
			WeatherController controller = WeatherControllerManager.forWorld(world);
			controller.onPlayerJoin((ServerPlayerEntity) player);
		}
	}

	@SubscribeEvent
	public static void onWorldTick(TickEvent.WorldTickEvent event) {
		World world = event.world;
		if (world.isRemote || event.phase == TickEvent.Phase.END) {
			return;
		}

		WeatherController controller = WEATHER_CONTROLLERS.get(world.getDimensionKey());
		if (controller != null) {
			controller.tick();
		}
	}

	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event) {
		IWorld world = event.getWorld();
		if (world instanceof IServerWorld) {
			WEATHER_CONTROLLERS.remove(((IServerWorld) world).getWorld().getDimensionKey());
		}
	}
}
