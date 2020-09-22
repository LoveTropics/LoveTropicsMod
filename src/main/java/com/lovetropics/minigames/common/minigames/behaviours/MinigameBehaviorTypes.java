package com.lovetropics.minigames.common.minigames.behaviours;

import com.lovetropics.minigames.common.minigames.behaviours.instances.LoadMapMinigameBehaviour;
import com.lovetropics.minigames.common.minigames.behaviours.instances.PositionParticipantsMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.RespawnSpectatorMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.TimedMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide.WeatherEventsMinigameBehavior;
import com.lovetropics.minigames.common.minigames.weather.MinigameWeatherConfig;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class MinigameBehaviorTypes
{
	public static final DeferredRegister<IMinigameBehaviorType<?>> MINIGAME_BEHAVIOURS_REGISTER = DeferredRegister.create(IMinigameBehaviorType.wildcardType(), "ltminigames");
	public static final Supplier<IForgeRegistry<IMinigameBehaviorType<?>>> MINIGAME_BEHAVIOURS_REGISTRY;

	public static final RegistryObject<IMinigameBehaviorType<PositionParticipantsMinigameBehavior>> POSITION_PARTICIPANTS;
	public static final RegistryObject<IMinigameBehaviorType<LoadMapMinigameBehaviour>> LOAD_MAP;
	public static final RegistryObject<IMinigameBehaviorType<WeatherEventsMinigameBehavior>> WEATHER_EVENTS;
	public static final RegistryObject<IMinigameBehaviorType<TimedMinigameBehavior>> TIMED;
	public static final RegistryObject<IMinigameBehaviorType<RespawnSpectatorMinigameBehavior>> RESPAWN_SPECTATOR;

	public static <T extends IMinigameBehavior> RegistryObject<IMinigameBehaviorType<T>> register(final String name, final MinigameBehaviorType.Factory<T> instanceFactory) {
		return MINIGAME_BEHAVIOURS_REGISTER.register(name, () -> new MinigameBehaviorType<>(instanceFactory));
	}

	public static <T extends IMinigameBehavior> RegistryObject<IMinigameBehaviorType<T>> registerInstance(final String name, final T instance) {
		return register(name, new MinigameBehaviorType.Factory<T>() {
			@Override
			public <D> T create(Dynamic<D> data) {
				return instance;
			}
		});
	}

	private static <T> WeatherEventsMinigameBehavior weatherEvents(Dynamic<T> root) {
		return new WeatherEventsMinigameBehavior(MinigameWeatherConfig.deserialize(root));
	}

	private static <T> PositionParticipantsMinigameBehavior positionParticipants(Dynamic<T> root) {
		BlockPos[] spawnPositions = root.get("positions").asList(BlockPos::deserialize).toArray(new BlockPos[0]);
		return new PositionParticipantsMinigameBehavior(spawnPositions);
	}

	private static <T> LoadMapMinigameBehaviour loadMap(Dynamic<T> root) {
		DimensionType dimension = DimensionType.byName(new ResourceLocation(root.get("dimension").asString("")));
		String loadFrom = root.get("load_from").asString("");
		String saveTo = root.get("save_to").asString("");

		return new LoadMapMinigameBehaviour(loadFrom);
	}

	private static <T> TimedMinigameBehavior timed(Dynamic<T> root) {
		long length = root.get("length").asLong(20 * 60);
		return new TimedMinigameBehavior(length);
	}

	static {
		MINIGAME_BEHAVIOURS_REGISTRY = MINIGAME_BEHAVIOURS_REGISTER.makeRegistry("minigame_behaviours", RegistryBuilder::new);

		POSITION_PARTICIPANTS = register("position_participants", MinigameBehaviorTypes::positionParticipants);
		LOAD_MAP = register("load_map", MinigameBehaviorTypes::loadMap);
		WEATHER_EVENTS = register("weather_events", MinigameBehaviorTypes::weatherEvents);
		TIMED = register("timed", MinigameBehaviorTypes::timed);
		RESPAWN_SPECTATOR = registerInstance("respawn_spectator", RespawnSpectatorMinigameBehavior.INSTANCE);
	}
}
