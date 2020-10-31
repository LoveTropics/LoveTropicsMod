package com.lovetropics.minigames.common.minigames.behaviours;

import com.lovetropics.minigames.common.minigames.behaviours.instances.*;
import com.lovetropics.minigames.common.minigames.behaviours.instances.conservation_exploration.RecordCreaturesBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.conservation_exploration.SpawnCreaturesBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.statistics.*;
import com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide.*;
import com.lovetropics.minigames.common.minigames.behaviours.instances.trash_dive.PlaceTrashBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.trash_dive.TrashCollectionBehavior;
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
	public static final RegistryObject<IMinigameBehaviorType<CommandEventsBehavior>> COMMANDS;
	public static final RegistryObject<IMinigameBehaviorType<IsolatePlayerStateBehavior>> ISOLATE_PLAYER_STATE;
	public static final RegistryObject<IMinigameBehaviorType<SetGameTypesBehavior>> SET_GAME_TYPES;
	public static final RegistryObject<IMinigameBehaviorType<PhasesMinigameBehavior>> PHASES;
	public static final RegistryObject<IMinigameBehaviorType<RisingTidesMinigameBehavior>> RISING_TIDES;
	public static final RegistryObject<IMinigameBehaviorType<ScheduledMessagesBehavior>> SCHEDULED_MESSAGES;
	public static final RegistryObject<IMinigameBehaviorType<WorldBorderMinigameBehavior>> WORLD_BORDER;
	public static final RegistryObject<IMinigameBehaviorType<SttWinConditionBehavior>> SURVIVE_THE_TIDE_INDIVIDUALS_WIN_CONDITION;
	public static final RegistryObject<IMinigameBehaviorType<SttWinConditionBehavior>> SURVIVE_THE_TIDE_TEAMS_WIN_CONDITION;
	public static final RegistryObject<IMinigameBehaviorType<FireworksOnDeathBehavior>> FIREWORKS_ON_DEATH;
	public static final RegistryObject<IMinigameBehaviorType<SurviveTheTideRulesetBehavior>> SURVIVE_THE_TIDE_RULESET;
	public static final RegistryObject<IMinigameBehaviorType<BindControlsBehavior>> BIND_CONTROLS;
	public static final RegistryObject<IMinigameBehaviorType<CancelPlayerDamageBehavior>> CANCEL_PLAYER_DAMAGE;
	public static final RegistryObject<IMinigameBehaviorType<SpawnCreaturesBehavior>> SPAWN_CREATURES;
	public static final RegistryObject<IMinigameBehaviorType<RecordCreaturesBehavior>> RECORD_CREATURES;
	public static final RegistryObject<IMinigameBehaviorType<SetGameRulesBehavior>> SET_GAME_RULES;
	public static final RegistryObject<IMinigameBehaviorType<PlaceTrashBehavior>> PLACE_TRASH;
	public static final RegistryObject<IMinigameBehaviorType<TrashCollectionBehavior>> TRASH_COLLECTION;
	public static final RegistryObject<IMinigameBehaviorType<TeamsBehavior>> TEAMS;
	public static final RegistryObject<IMinigameBehaviorType<SpectatorChaseBehavior>> SPECTATOR_CHASE;
	public static final RegistryObject<IMinigameBehaviorType<ForceLoadRegionBehavior>> FORCE_LOAD_REGION;
	public static final RegistryObject<IMinigameBehaviorType<DeleteBlocksBehavior>> DELETE_BLOCKS;
	public static final RegistryObject<IMinigameBehaviorType<EliminatePlayerControlBehavior>> ELIMINATE_PLAYER_CONTROL;

	public static final RegistryObject<IMinigameBehaviorType<BindObjectiveToStatisticBehavior>> BIND_OBJECTIVE_TO_STATISTIC;
	public static final RegistryObject<IMinigameBehaviorType<PlaceByStatisticBehavior>> PLACE_BY_STATISTIC;
	public static final RegistryObject<IMinigameBehaviorType<PlaceByDeathOrderBehavior>> PLACE_BY_DEATH_ORDER;
	public static final RegistryObject<IMinigameBehaviorType<CampingTrackerBehavior>> CAMPING_TRACKER;
	public static final RegistryObject<IMinigameBehaviorType<CauseOfDeathTrackerBehavior>> CAUSE_OF_DEATH_TRACKER;
	public static final RegistryObject<IMinigameBehaviorType<KillsTrackerBehavior>> KILLS_TRACKER;
	public static final RegistryObject<IMinigameBehaviorType<TimeSurvivedTrackerBehavior>> TIME_SURVIVED_TRACKER;
	public static final RegistryObject<IMinigameBehaviorType<DamageTrackerBehavior>> DAMAGE_TRACKER;
	public static final RegistryObject<IMinigameBehaviorType<BlocksBrokenTrackerBehavior>> BLOCKS_BROKEN_TRACKER;

	public static final RegistryObject<IMinigameBehaviorType<DisplayLeaderboardOnFinishBehavior<?>>> DISPLAY_LEADERBOARD_ON_FINISH;

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
		COMMANDS = register("commands", CommandEventsBehavior::parse);
		ISOLATE_PLAYER_STATE = register("isolate_player_state", IsolatePlayerStateBehavior::parse);
		SET_GAME_TYPES = register("set_game_types", SetGameTypesBehavior::parse);
		PHASES = register("phases", PhasesMinigameBehavior::parse);
		RISING_TIDES = register("rising_tides", RisingTidesMinigameBehavior::parse);
		SCHEDULED_MESSAGES = register("scheduled_messages", ScheduledMessagesBehavior::parse);
		WORLD_BORDER = register("world_border", WorldBorderMinigameBehavior::parse);
		SURVIVE_THE_TIDE_INDIVIDUALS_WIN_CONDITION = register("survive_the_tide_individuals_win_condition", SttIndividualsWinConditionBehavior::parse);
		SURVIVE_THE_TIDE_TEAMS_WIN_CONDITION = register("survive_the_tide_teams_win_condition", SttTeamsWinConditionBehavior::parse);
		FIREWORKS_ON_DEATH = register("fireworks_on_death", FireworksOnDeathBehavior::parse);
		SURVIVE_THE_TIDE_RULESET = register("survive_the_tide_ruleset", SurviveTheTideRulesetBehavior::parse);
		BIND_CONTROLS = register("bind_controls", BindControlsBehavior::parse);
		CANCEL_PLAYER_DAMAGE = register("cancel_player_damage", CancelPlayerDamageBehavior::parse);
		SET_GAME_RULES = register("set_game_rules", SetGameRulesBehavior::parse);
		TEAMS = register("teams", TeamsBehavior::parse);
		SPAWN_CREATURES = register("spawn_creatures", SpawnCreaturesBehavior::parse);
		RECORD_CREATURES = register("record_creatures", RecordCreaturesBehavior::parse);
		PLACE_TRASH = register("place_trash", PlaceTrashBehavior::parse);
		TRASH_COLLECTION = register("trash_collection", TrashCollectionBehavior::parse);
		SPECTATOR_CHASE = register("spectator_chase", SpectatorChaseBehavior::parse);
		FORCE_LOAD_REGION = register("force_load_region", ForceLoadRegionBehavior::parse);
		DELETE_BLOCKS = register("delete_blocks", DeleteBlocksBehavior::parse);
		ELIMINATE_PLAYER_CONTROL = register("eliminate_player_control", EliminatePlayerControlBehavior::parse);

		BIND_OBJECTIVE_TO_STATISTIC = register("bind_objective_to_statistic", BindObjectiveToStatisticBehavior::parse);
		PLACE_BY_STATISTIC = register("place_by_statistic", PlaceByStatisticBehavior::parse);
		PLACE_BY_DEATH_ORDER = register("place_by_death_order", PlaceByDeathOrderBehavior::parse);
		CAMPING_TRACKER = register("camping_tracker", CampingTrackerBehavior::parse);
		CAUSE_OF_DEATH_TRACKER = register("cause_of_death_tracker", CauseOfDeathTrackerBehavior::parse);
		KILLS_TRACKER = register("kills_tracker", KillsTrackerBehavior::parse);
		TIME_SURVIVED_TRACKER = register("time_survived_tracker", TimeSurvivedTrackerBehavior::parse);
		DAMAGE_TRACKER = register("damage_tracker", DamageTrackerBehavior::parse);
		BLOCKS_BROKEN_TRACKER = register("blocks_broken_tracker", BlocksBrokenTrackerBehavior::parse);

		DISPLAY_LEADERBOARD_ON_FINISH = register("display_leaderboard_on_finish", DisplayLeaderboardOnFinishBehavior::parse);
	}
}
