package com.lovetropics.minigames.common.minigames.behaviours;

import com.lovetropics.minigames.common.minigames.behaviours.instances.CommandInvokeBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.FireworksOnDeathBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.IsolatePlayerStateBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.PhasesMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.PositionPlayersMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.RespawnSpectatorMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.ScheduledMessagesBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.SetGameTypesBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.TimedMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide.RisingTidesMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide.SurviveTheTideRulesetBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide.SurviveTheTideWinConditionBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide.WeatherEventsMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide.WorldBorderMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class MinigameBehaviorTypes {
	public static final DeferredRegister<IMinigameBehaviorType<?>> MINIGAME_BEHAVIOURS_REGISTER = DeferredRegister.create(IMinigameBehaviorType.wildcardType(), "ltminigames");
	public static final Supplier<IForgeRegistry<IMinigameBehaviorType<?>>> MINIGAME_BEHAVIOURS_REGISTRY;

	public static final RegistryObject<IMinigameBehaviorType<PositionPlayersMinigameBehavior>> POSITION_PLAYERS;
	public static final RegistryObject<IMinigameBehaviorType<WeatherEventsMinigameBehavior>> WEATHER_EVENTS;
	public static final RegistryObject<IMinigameBehaviorType<TimedMinigameBehavior>> TIMED;
	public static final RegistryObject<IMinigameBehaviorType<RespawnSpectatorMinigameBehavior>> RESPAWN_SPECTATOR;
	public static final RegistryObject<IMinigameBehaviorType<CommandInvokeBehavior>> COMMANDS;
	public static final RegistryObject<IMinigameBehaviorType<IsolatePlayerStateBehavior>> ISOLATE_PLAYER_STATE;
	public static final RegistryObject<IMinigameBehaviorType<SetGameTypesBehavior>> SET_GAME_TYPES;
	public static final RegistryObject<IMinigameBehaviorType<PhasesMinigameBehavior>> PHASES;
	public static final RegistryObject<IMinigameBehaviorType<RisingTidesMinigameBehavior>> RISING_TIDES;
	public static final RegistryObject<IMinigameBehaviorType<ScheduledMessagesBehavior>> SCHEDULED_MESSAGES;
	public static final RegistryObject<IMinigameBehaviorType<WorldBorderMinigameBehavior>> WORLD_BORDER;
	public static final RegistryObject<IMinigameBehaviorType<SurviveTheTideWinConditionBehavior>> SURVIVE_THE_TIDE_WIN_CONDITION;
	public static final RegistryObject<IMinigameBehaviorType<FireworksOnDeathBehavior>> FIREWORKS_ON_DEATH;
	public static final RegistryObject<IMinigameBehaviorType<SurviveTheTideRulesetBehavior>> SURVIVE_THE_TIDE_RULESET;

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

	static {
		MINIGAME_BEHAVIOURS_REGISTRY = MINIGAME_BEHAVIOURS_REGISTER.makeRegistry("minigame_behaviours", RegistryBuilder::new);

		POSITION_PLAYERS = register("position_players", PositionPlayersMinigameBehavior::parse);
		WEATHER_EVENTS = register("weather_events", WeatherEventsMinigameBehavior::parse);
		TIMED = register("timed", TimedMinigameBehavior::parse);
		RESPAWN_SPECTATOR = registerInstance("respawn_spectator", RespawnSpectatorMinigameBehavior.INSTANCE);
		COMMANDS = register("commands", CommandInvokeBehavior::parse);
		ISOLATE_PLAYER_STATE = register("isolate_player_state", IsolatePlayerStateBehavior::parse);
		SET_GAME_TYPES = register("set_game_types", SetGameTypesBehavior::parse);
		PHASES = register("phases", PhasesMinigameBehavior::parse);
		RISING_TIDES = register("rising_tides", RisingTidesMinigameBehavior::parse);
		SCHEDULED_MESSAGES = register("scheduled_messages", ScheduledMessagesBehavior::parse);
		WORLD_BORDER = register("world_border", WorldBorderMinigameBehavior::parse);
		SURVIVE_THE_TIDE_WIN_CONDITION = register("survive_the_tide_win_condition", SurviveTheTideWinConditionBehavior::parse);
		FIREWORKS_ON_DEATH = register("fireworks_on_death", FireworksOnDeathBehavior::parse);
		SURVIVE_THE_TIDE_RULESET = register("survive_the_tide_ruleset", SurviveTheTideRulesetBehavior::parse);
	}
}
