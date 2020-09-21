package com.lovetropics.minigames.common.minigames.behaviours;

import com.google.gson.JsonElement;
import com.lovetropics.minigames.common.minigames.behaviours.instances.LoadMapMinigameBehaviour;
import com.lovetropics.minigames.common.minigames.behaviours.instances.PositionParticipantsMinigameBehavior;
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

import java.util.function.Function;
import java.util.function.Supplier;

public class MinigameBehaviorTypes
{
	@SuppressWarnings("unchecked")
	public static final DeferredRegister<IMinigameBehaviorType<?>> MINIGAME_BEHAVIOURS_REGISTER = DeferredRegister.create(IMinigameBehaviorType.class, "ltminigames");
	public static final Supplier<IForgeRegistry<IMinigameBehaviorType<?>>> MINIGAME_BEHAVIOURS_REGISTRY;

	public static final RegistryObject<IMinigameBehaviorType<PositionParticipantsMinigameBehavior>> POSITION_PARTICIPANTS;
	public static final RegistryObject<IMinigameBehaviorType<LoadMapMinigameBehaviour>> LOAD_MAP;
	public static final RegistryObject<IMinigameBehaviorType<WeatherEventsMinigameBehavior>> WEATHER_EVENTS;

	public static <T extends IMinigameBehavior> RegistryObject<IMinigameBehaviorType<T>> register(final String name, final Function<Dynamic<JsonElement>, T> instanceFactory) {
		final ResourceLocation id = new ResourceLocation("ltminigames", name);
		return MINIGAME_BEHAVIOURS_REGISTER.register(name, () -> new MinigameBehaviorType<>(id, instanceFactory));
	}

	private static WeatherEventsMinigameBehavior weatherEvents(Dynamic<JsonElement> root) {
		return new WeatherEventsMinigameBehavior(MinigameWeatherConfig.deserialize(root));
	}

	private static PositionParticipantsMinigameBehavior positionParticipants(Dynamic<JsonElement> root) {
		BlockPos[] spawnPositions = root.get("positions").asList(BlockPos::deserialize).toArray(new BlockPos[0]);
		return new PositionParticipantsMinigameBehavior(spawnPositions);
	}

	private static LoadMapMinigameBehaviour loadMap(Dynamic<JsonElement> root) {
		DimensionType dimension = DimensionType.byName(new ResourceLocation(root.get("dimension").asString("")));
		String loadFrom = root.get("load_from").asString("");
		String saveTo = root.get("save_to").asString("");

		return new LoadMapMinigameBehaviour(loadFrom);
	}

	static {
		MINIGAME_BEHAVIOURS_REGISTRY = MINIGAME_BEHAVIOURS_REGISTER.makeRegistry("minigame_behaviours", RegistryBuilder::new);

		POSITION_PARTICIPANTS = register("position_participants", MinigameBehaviorTypes::positionParticipants);
		LOAD_MAP = register("load_map", MinigameBehaviorTypes::loadMap);
		WEATHER_EVENTS = register("weather_events", MinigameBehaviorTypes::weatherEvents);
	}
}
