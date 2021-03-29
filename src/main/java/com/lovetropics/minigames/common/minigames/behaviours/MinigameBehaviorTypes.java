package com.lovetropics.minigames.common.minigames.behaviours;

import com.lovetropics.minigames.common.minigames.behaviours.instances.*;
import com.lovetropics.minigames.common.minigames.behaviours.instances.build_competition.PollFinalistsBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.conservation_exploration.ConservationExplorationBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.donations.*;
import com.lovetropics.minigames.common.minigames.behaviours.instances.statistics.*;
import com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide.*;
import com.lovetropics.minigames.common.minigames.behaviours.instances.trash_dive.PlaceTrashBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.instances.trash_dive.TrashCollectionBehavior;
import com.mojang.serialization.Codec;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class MinigameBehaviorTypes {
	public static final DeferredRegister<MinigameBehaviorType<?>> MINIGAME_BEHAVIOURS_REGISTER = DeferredRegister.create(MinigameBehaviorType.wildcardType(), "ltminigames");
	public static final Supplier<IForgeRegistry<MinigameBehaviorType<?>>> MINIGAME_BEHAVIOURS_REGISTRY;

	public static final RegistryObject<MinigameBehaviorType<PositionPlayersMinigameBehavior>> POSITION_PLAYERS;
	public static final RegistryObject<MinigameBehaviorType<SurviveTheTideWeatherBehavior>> WEATHER_EVENTS;
	public static final RegistryObject<MinigameBehaviorType<TimedMinigameBehavior>> TIMED;
	public static final RegistryObject<MinigameBehaviorType<RespawnSpectatorMinigameBehavior>> RESPAWN_SPECTATOR;
	public static final RegistryObject<MinigameBehaviorType<CommandEventsBehavior>> COMMANDS;
	public static final RegistryObject<MinigameBehaviorType<IsolatePlayerStateBehavior>> ISOLATE_PLAYER_STATE;
	public static final RegistryObject<MinigameBehaviorType<SetGameTypesBehavior>> SET_GAME_TYPES;
	public static final RegistryObject<MinigameBehaviorType<PhasesMinigameBehavior>> PHASES;
	public static final RegistryObject<MinigameBehaviorType<RisingTidesMinigameBehavior>> RISING_TIDES;
	public static final RegistryObject<MinigameBehaviorType<ScheduledMessagesBehavior>> SCHEDULED_MESSAGES;
	public static final RegistryObject<MinigameBehaviorType<WorldBorderMinigameBehavior>> WORLD_BORDER;
	public static final RegistryObject<MinigameBehaviorType<SttIndividualsWinConditionBehavior>> SURVIVE_THE_TIDE_INDIVIDUALS_WIN_CONDITION;
	public static final RegistryObject<MinigameBehaviorType<SttTeamsWinConditionBehavior>> SURVIVE_THE_TIDE_TEAMS_WIN_CONDITION;
	public static final RegistryObject<MinigameBehaviorType<FireworksOnDeathBehavior>> FIREWORKS_ON_DEATH;
	public static final RegistryObject<MinigameBehaviorType<SurviveTheTideRulesetBehavior>> SURVIVE_THE_TIDE_RULESET;
	public static final RegistryObject<MinigameBehaviorType<BindControlsBehavior>> BIND_CONTROLS;
	public static final RegistryObject<MinigameBehaviorType<CancelPlayerDamageBehavior>> CANCEL_PLAYER_DAMAGE;
	public static final RegistryObject<MinigameBehaviorType<ConservationExplorationBehavior>> CONSERVATION_EXPLORATION;
	public static final RegistryObject<MinigameBehaviorType<SetGameRulesBehavior>> SET_GAME_RULES;
	public static final RegistryObject<MinigameBehaviorType<PlaceTrashBehavior>> PLACE_TRASH;
	public static final RegistryObject<MinigameBehaviorType<TrashCollectionBehavior>> TRASH_COLLECTION;
	public static final RegistryObject<MinigameBehaviorType<TeamsBehavior>> TEAMS;
	public static final RegistryObject<MinigameBehaviorType<SpectatorChaseBehavior>> SPECTATOR_CHASE;
	public static final RegistryObject<MinigameBehaviorType<ForceLoadRegionBehavior>> FORCE_LOAD_REGION;
	public static final RegistryObject<MinigameBehaviorType<DeleteBlocksBehavior>> DELETE_BLOCKS;
	public static final RegistryObject<MinigameBehaviorType<EliminatePlayerControlBehavior>> ELIMINATE_PLAYER_CONTROL;
	public static final RegistryObject<MinigameBehaviorType<PlaceSttChestsMinigameBehavior>> PLACE_STT_CHESTS;
	public static final RegistryObject<MinigameBehaviorType<GenerateEntitiesBehavior>> GENERATE_ENTITIES;
	public static final RegistryObject<MinigameBehaviorType<WeatherControlsBehavior>> WEATHER_CONTROLS;
	public static final RegistryObject<MinigameBehaviorType<RunCommandInRegionBehavior>> RUN_COMMAND_IN_REGION;
	public static final RegistryObject<MinigameBehaviorType<TNTAutoFuseBehavior>> TNT_AUTO_FUSE;
	public static final RegistryObject<MinigameBehaviorType<DisableHungerBehavior>> DISABLE_HUNGER;
	public static final RegistryObject<MinigameBehaviorType<DisableTNTBlockDestructionBehavior>> DISABLE_TNT_BLOCK_DESTRUCTION;
	public static final RegistryObject<MinigameBehaviorType<RevealPlayersBehavior>> REVEAL_PLAYERS;
	public static final RegistryObject<MinigameBehaviorType<SetMaxHealthBehavior>> SET_MAX_HEALTH;

	public static final RegistryObject<MinigameBehaviorType<BindObjectiveToStatisticBehavior>> BIND_OBJECTIVE_TO_STATISTIC;
	public static final RegistryObject<MinigameBehaviorType<PlaceByStatisticBehavior>> PLACE_BY_STATISTIC;
	public static final RegistryObject<MinigameBehaviorType<PlaceByDeathOrderBehavior>> PLACE_BY_DEATH_ORDER;
	public static final RegistryObject<MinigameBehaviorType<CampingTrackerBehavior>> CAMPING_TRACKER;
	public static final RegistryObject<MinigameBehaviorType<CauseOfDeathTrackerBehavior>> CAUSE_OF_DEATH_TRACKER;
	public static final RegistryObject<MinigameBehaviorType<KillsTrackerBehavior>> KILLS_TRACKER;
	public static final RegistryObject<MinigameBehaviorType<TimeSurvivedTrackerBehavior>> TIME_SURVIVED_TRACKER;
	public static final RegistryObject<MinigameBehaviorType<DamageTrackerBehavior>> DAMAGE_TRACKER;
	public static final RegistryObject<MinigameBehaviorType<BlocksBrokenTrackerBehavior>> BLOCKS_BROKEN_TRACKER;

	public static final RegistryObject<MinigameBehaviorType<DisplayLeaderboardOnFinishBehavior<?>>> DISPLAY_LEADERBOARD_ON_FINISH;
	public static final RegistryObject<MinigameBehaviorType<LootPackageBehavior>> LOOT_PACKAGE;
	public static final RegistryObject<MinigameBehaviorType<EffectPackageBehavior>> EFFECT_PACKAGE;
	public static final RegistryObject<MinigameBehaviorType<SwapPlayersPackageBehavior>> SWAP_PLAYERS_PACKAGE;
	public static final RegistryObject<MinigameBehaviorType<SpawnEntityAtPlayerPackageBehavior>> SPAWN_ENTITY_AT_PLAYER_PACKAGE;
	public static final RegistryObject<MinigameBehaviorType<SpawnEntityAtRegionsPackageBehavior>> SPAWN_ENTITY_AT_REGIONS_PACKAGE;
	public static final RegistryObject<MinigameBehaviorType<SpawnEntitiesAroundPlayersPackageBehavior>> SPAWN_ENTITIES_AROUND_PLAYERS_PACKAGE;
	public static final RegistryObject<MinigameBehaviorType<SpawnEntitiesAtRegionsOverTimePackageBehavior>> SPAWN_ENTITIES_AT_REGIONS_OVER_TIME_PACKAGE;
	public static final RegistryObject<MinigameBehaviorType<SetBlockAtPlayerPackageBehavior>> SET_BLOCK_AT_PLAYER_PACKAGE;
	public static final RegistryObject<MinigameBehaviorType<ForcedPlayerHeadPackageBehavior>> FORCED_PLAYER_HEAD_PACKAGE;
	public static final RegistryObject<MinigameBehaviorType<PufferfishPackageBehavior>> PUFFERFISH_PACKAGE;
	public static final RegistryObject<MinigameBehaviorType<ShootProjectilesAroundAllPlayersPackageBehavior>> SHOOT_PROJECTILES_AT_ALL_PLAYERS;
	public static final RegistryObject<MinigameBehaviorType<ShootProjectilesAroundPlayerPackageBehavior>> SHOOT_PROJECTILES_AT_PLAYER;

	public static final RegistryObject<MinigameBehaviorType<PollFinalistsBehavior>> POLL_FINALISTS;

	public static <T extends IMinigameBehavior> RegistryObject<MinigameBehaviorType<T>> register(final String name, final Codec<T> codec) {
		return MINIGAME_BEHAVIOURS_REGISTER.register(name, () -> new MinigameBehaviorType<>(codec));
	}

	static {
		MINIGAME_BEHAVIOURS_REGISTRY = MINIGAME_BEHAVIOURS_REGISTER.makeRegistry("minigame_behaviours", () -> {
			return new RegistryBuilder<MinigameBehaviorType<?>>()
					.disableSync()
					.disableSaving();
		});

		POSITION_PLAYERS = register("position_players", PositionPlayersMinigameBehavior.CODEC);
		WEATHER_EVENTS = register("weather_events", SurviveTheTideWeatherBehavior.CODEC);
		TIMED = register("timed", TimedMinigameBehavior.CODEC);
		RESPAWN_SPECTATOR = register("respawn_spectator", RespawnSpectatorMinigameBehavior.CODEC);
		COMMANDS = register("commands", CommandEventsBehavior.CODEC);
		ISOLATE_PLAYER_STATE = register("isolate_player_state", IsolatePlayerStateBehavior.CODEC);
		SET_GAME_TYPES = register("set_game_types", SetGameTypesBehavior.CODEC);
		PHASES = register("phases", PhasesMinigameBehavior.CODEC);
		RISING_TIDES = register("rising_tides", RisingTidesMinigameBehavior.CODEC);
		SCHEDULED_MESSAGES = register("scheduled_messages", ScheduledMessagesBehavior.CODEC);
		WORLD_BORDER = register("world_border", WorldBorderMinigameBehavior.CODEC);
		SURVIVE_THE_TIDE_INDIVIDUALS_WIN_CONDITION = register("survive_the_tide_individuals_win_condition", SttIndividualsWinConditionBehavior.CODEC);
		SURVIVE_THE_TIDE_TEAMS_WIN_CONDITION = register("survive_the_tide_teams_win_condition", SttTeamsWinConditionBehavior.CODEC);
		FIREWORKS_ON_DEATH = register("fireworks_on_death", FireworksOnDeathBehavior.CODEC);
		SURVIVE_THE_TIDE_RULESET = register("survive_the_tide_ruleset", SurviveTheTideRulesetBehavior.CODEC);
		BIND_CONTROLS = register("bind_controls", BindControlsBehavior.CODEC);
		CANCEL_PLAYER_DAMAGE = register("cancel_player_damage", CancelPlayerDamageBehavior.CODEC);
		SET_GAME_RULES = register("set_game_rules", SetGameRulesBehavior.CODEC);
		TEAMS = register("teams", TeamsBehavior.CODEC);
		CONSERVATION_EXPLORATION = register("conservation_exploration", ConservationExplorationBehavior.CODEC);
		PLACE_TRASH = register("place_trash", PlaceTrashBehavior.CODEC);
		TRASH_COLLECTION = register("trash_collection", TrashCollectionBehavior.CODEC);
		SPECTATOR_CHASE = register("spectator_chase", SpectatorChaseBehavior.CODEC);
		FORCE_LOAD_REGION = register("force_load_region", ForceLoadRegionBehavior.CODEC);
		DELETE_BLOCKS = register("delete_blocks", DeleteBlocksBehavior.CODEC);
		ELIMINATE_PLAYER_CONTROL = register("eliminate_player_control", EliminatePlayerControlBehavior.CODEC);
		PLACE_STT_CHESTS = register("place_stt_chests", PlaceSttChestsMinigameBehavior.CODEC);
		GENERATE_ENTITIES = register("generate_entities", GenerateEntitiesBehavior.CODEC);
		WEATHER_CONTROLS = register("weather_controls", WeatherControlsBehavior.CODEC);
		RUN_COMMAND_IN_REGION = register("run_command_in_region", RunCommandInRegionBehavior.CODEC);
        TNT_AUTO_FUSE = register("tnt_auto_fuse", TNTAutoFuseBehavior.CODEC);
		DISABLE_HUNGER = register("disable_hunger", DisableHungerBehavior.CODEC);
		DISABLE_TNT_BLOCK_DESTRUCTION = register("disable_tnt_block_destruction", DisableTNTBlockDestructionBehavior.CODEC);
		REVEAL_PLAYERS = register("reveal_players", RevealPlayersBehavior.CODEC);
		SET_MAX_HEALTH = register("set_max_health", SetMaxHealthBehavior.CODEC);

		BIND_OBJECTIVE_TO_STATISTIC = register("bind_objective_to_statistic", BindObjectiveToStatisticBehavior.CODEC);
		PLACE_BY_STATISTIC = register("place_by_statistic", PlaceByStatisticBehavior.CODEC);
		PLACE_BY_DEATH_ORDER = register("place_by_death_order", PlaceByDeathOrderBehavior.CODEC);
		CAMPING_TRACKER = register("camping_tracker", CampingTrackerBehavior.CODEC);
		CAUSE_OF_DEATH_TRACKER = register("cause_of_death_tracker", CauseOfDeathTrackerBehavior.CODEC);
		KILLS_TRACKER = register("kills_tracker", KillsTrackerBehavior.CODEC);
		TIME_SURVIVED_TRACKER = register("time_survived_tracker", TimeSurvivedTrackerBehavior.CODEC);
		DAMAGE_TRACKER = register("damage_tracker", DamageTrackerBehavior.CODEC);
		BLOCKS_BROKEN_TRACKER = register("blocks_broken_tracker", BlocksBrokenTrackerBehavior.CODEC);

		DISPLAY_LEADERBOARD_ON_FINISH = register("display_leaderboard_on_finish", DisplayLeaderboardOnFinishBehavior.CODEC);
		LOOT_PACKAGE = register("loot_package", LootPackageBehavior.CODEC);
		EFFECT_PACKAGE = register("effect_package", EffectPackageBehavior.CODEC);
		SWAP_PLAYERS_PACKAGE = register("swap_players_package", SwapPlayersPackageBehavior.CODEC);
		SPAWN_ENTITY_AT_PLAYER_PACKAGE = register("spawn_entity_at_player_package", SpawnEntityAtPlayerPackageBehavior.CODEC);
		SPAWN_ENTITY_AT_REGIONS_PACKAGE = register("spawn_entity_at_regions_package", SpawnEntityAtRegionsPackageBehavior.CODEC);
		SPAWN_ENTITIES_AROUND_PLAYERS_PACKAGE = register("spawn_entities_around_players_package", SpawnEntitiesAroundPlayersPackageBehavior.CODEC);
		SPAWN_ENTITIES_AT_REGIONS_OVER_TIME_PACKAGE = register("spawn_entities_at_regions_over_time_package", SpawnEntitiesAtRegionsOverTimePackageBehavior.CODEC);
		SET_BLOCK_AT_PLAYER_PACKAGE = register("set_block_at_player_package", SetBlockAtPlayerPackageBehavior.CODEC);
		FORCED_PLAYER_HEAD_PACKAGE = register("forced_player_head_package", ForcedPlayerHeadPackageBehavior.CODEC);
		PUFFERFISH_PACKAGE = register("pufferfish_package", PufferfishPackageBehavior.CODEC);
		SHOOT_PROJECTILES_AT_ALL_PLAYERS = register("shoot_projectiles_at_all_players", ShootProjectilesAroundAllPlayersPackageBehavior.CODEC);
		SHOOT_PROJECTILES_AT_PLAYER = register("shoot_projectiles_at_player", ShootProjectilesAroundPlayerPackageBehavior.CODEC);

		POLL_FINALISTS = register("poll_finalists", PollFinalistsBehavior.CODEC);
	}
}
