package com.lovetropics.minigames.common.minigames.behaviours;

import com.lovetropics.minigames.common.minigames.behaviours.instances.CommandInvokeBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.IsolatePlayerStateBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.LoadMapMinigameBehaviour;
import com.lovetropics.minigames.common.minigames.behaviours.instances.PositionPlayersMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.RespawnSpectatorMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.SetGameTypesBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.TimedMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide.WeatherEventsMinigameBehavior;
import com.lovetropics.minigames.common.minigames.weather.MinigameWeatherConfig;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class MinigameBehaviorTypes {
	public static final DeferredRegister<IMinigameBehaviorType<?>> MINIGAME_BEHAVIOURS_REGISTER = DeferredRegister.create(IMinigameBehaviorType.wildcardType(), "ltminigames");
	public static final Supplier<IForgeRegistry<IMinigameBehaviorType<?>>> MINIGAME_BEHAVIOURS_REGISTRY;

	public static final RegistryObject<IMinigameBehaviorType<PositionPlayersMinigameBehavior>> POSITION_PLAYERS;
	public static final RegistryObject<IMinigameBehaviorType<LoadMapMinigameBehaviour>> LOAD_MAP;
	public static final RegistryObject<IMinigameBehaviorType<WeatherEventsMinigameBehavior>> WEATHER_EVENTS;
	public static final RegistryObject<IMinigameBehaviorType<TimedMinigameBehavior>> TIMED;
	public static final RegistryObject<IMinigameBehaviorType<RespawnSpectatorMinigameBehavior>> RESPAWN_SPECTATOR;
	public static final RegistryObject<IMinigameBehaviorType<CommandInvokeBehavior>> COMMANDS;
	public static final RegistryObject<IMinigameBehaviorType<IsolatePlayerStateBehavior>> ISOLATE_PLAYER_STATE;
	public static final RegistryObject<IMinigameBehaviorType<SetGameTypesBehavior>> SET_GAME_TYPES;

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

	private static <T> PositionPlayersMinigameBehavior positionPlayers(Dynamic<T> root) {
		String[] participantSpawns = root.get("participants").asList(d -> d.asString("")).toArray(new String[0]);
		String[] spectatorSpawns = root.get("spectators").asList(d -> d.asString("")).toArray(new String[0]);
		return new PositionPlayersMinigameBehavior(participantSpawns, spectatorSpawns);
	}

	private static <T> LoadMapMinigameBehaviour loadMap(Dynamic<T> root) {
		ResourceLocation loadFrom = new ResourceLocation(root.get("load_from").asString(""));
		return new LoadMapMinigameBehaviour(loadFrom);
	}

	private static <T> TimedMinigameBehavior timed(Dynamic<T> root) {
		long length = root.get("length").asLong(20 * 60);
		return new TimedMinigameBehavior(length);
	}

	private static <T> IsolatePlayerStateBehavior isolatePlayerState(Dynamic<T> root) {
		return new IsolatePlayerStateBehavior();
	}

	private static <T> SetGameTypesBehavior setGameTypes(Dynamic<T> root) {
		GameType participant = GameType.getByName(root.get("participant").asString(""));
		GameType spectator = GameType.getByName(root.get("spectator").asString(""));
		return new SetGameTypesBehavior(participant, spectator);
	}

	static {
		MINIGAME_BEHAVIOURS_REGISTRY = MINIGAME_BEHAVIOURS_REGISTER.makeRegistry("minigame_behaviours", RegistryBuilder::new);

		POSITION_PLAYERS = register("position_players", MinigameBehaviorTypes::positionPlayers);
		LOAD_MAP = register("load_map", MinigameBehaviorTypes::loadMap);
		WEATHER_EVENTS = register("weather_events", MinigameBehaviorTypes::weatherEvents);
		TIMED = register("timed", MinigameBehaviorTypes::timed);
		RESPAWN_SPECTATOR = registerInstance("respawn_spectator", RespawnSpectatorMinigameBehavior.INSTANCE);
		COMMANDS = register("commands", CommandInvokeBehavior::parse);
		ISOLATE_PLAYER_STATE = register("isolate_player_state", MinigameBehaviorTypes::isolatePlayerState);
		SET_GAME_TYPES = register("set_game_types", MinigameBehaviorTypes::setGameTypes);
	}
}
