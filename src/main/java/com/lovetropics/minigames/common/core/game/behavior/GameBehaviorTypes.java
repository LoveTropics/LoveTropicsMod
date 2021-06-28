package com.lovetropics.minigames.common.core.game.behavior;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.behavior.instances.*;
import com.lovetropics.minigames.common.core.game.behavior.instances.command.*;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.*;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.*;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.TeamWinTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.TeamsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.*;
import com.lovetropics.minigames.common.util.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.LoveTropicsRegistrate;
import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class GameBehaviorTypes {
	public static final DeferredRegister<GameBehaviorType<?>> REGISTER = DeferredRegister.create(GameBehaviorType.type(), Constants.MODID);

	public static final Supplier<IForgeRegistry<GameBehaviorType<?>>> REGISTRY = REGISTER.makeRegistry("minigame_behaviours", () -> {
		return new RegistryBuilder<GameBehaviorType<?>>()
				.disableSync()
				.disableSaving();
	});

	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final Codec<GameBehaviorType<?>> TYPE_CODEC = MoreCodecs.ofForgeRegistry(REGISTRY);

	public static final GameBehaviorEntry<PositionPlayersBehavior> POSITION_PLAYERS;
	public static final GameBehaviorEntry<TimedGameBehavior> TIMED;
	public static final GameBehaviorEntry<RespawnSpectatorBehavior> RESPAWN_SPECTATOR;
	public static final GameBehaviorEntry<CommandEventsBehavior> COMMANDS;
	public static final GameBehaviorEntry<IsolatePlayerStateBehavior> ISOLATE_PLAYER_STATE;
	public static final GameBehaviorEntry<SetGameTypesBehavior> SET_GAME_TYPES;
	public static final GameBehaviorEntry<PhaseControllerBehavior> PHASE_CONTROLLER;
	public static final GameBehaviorEntry<ScheduledMessagesBehavior> SCHEDULED_MESSAGES;
	public static final GameBehaviorEntry<FireworksOnDeathBehavior> FIREWORKS_ON_DEATH;
	public static final GameBehaviorEntry<BindControlsBehavior> BIND_CONTROLS;
	public static final GameBehaviorEntry<CancelPlayerDamageBehavior> CANCEL_PLAYER_DAMAGE;
	public static final GameBehaviorEntry<SetGameRulesBehavior> SET_GAME_RULES;
	public static final GameBehaviorEntry<TeamsBehavior> TEAMS;
	public static final GameBehaviorEntry<SpectatorChaseBehavior> SPECTATOR_CHASE;
	public static final GameBehaviorEntry<ForceLoadRegionBehavior> FORCE_LOAD_REGION;
	public static final GameBehaviorEntry<SetBlocksBehavior> SET_BLOCKS;
	public static final GameBehaviorEntry<EliminatePlayerControlBehavior> ELIMINATE_PLAYER_CONTROL;
	public static final GameBehaviorEntry<FillChestsByMarkerBehavior> FILL_CHESTS_BY_MARKER;
	public static final GameBehaviorEntry<GenerateEntitiesBehavior> GENERATE_ENTITIES;
	public static final GameBehaviorEntry<WeatherControlsBehavior> WEATHER_CONTROLS;
	public static final GameBehaviorEntry<RunCommandInRegionBehavior> RUN_COMMAND_IN_REGION;
	public static final GameBehaviorEntry<TntAutoFuseBehavior> TNT_AUTO_FUSE;
	public static final GameBehaviorEntry<DisableHungerBehavior> DISABLE_HUNGER;
	public static final GameBehaviorEntry<DisableTntDestructionBehavior> DISABLE_TNT_BLOCK_DESTRUCTION;
	public static final GameBehaviorEntry<SetMaxHealthBehavior> SET_MAX_HEALTH;
	public static final GameBehaviorEntry<IndividualWinTrigger> INDIVIDUAL_WIN_TRIGGER;
	public static final GameBehaviorEntry<TeamWinTrigger> TEAM_WIN_TRIGGER;

	public static final GameBehaviorEntry<BindObjectiveToStatisticBehavior> BIND_OBJECTIVE_TO_STATISTIC;
	public static final GameBehaviorEntry<PlaceByStatisticBehavior> PLACE_BY_STATISTIC;
	public static final GameBehaviorEntry<PlaceByDeathOrderBehavior> PLACE_BY_DEATH_ORDER;
	public static final GameBehaviorEntry<CampingTrackerBehavior> CAMPING_TRACKER;
	public static final GameBehaviorEntry<CauseOfDeathTrackerBehavior> CAUSE_OF_DEATH_TRACKER;
	public static final GameBehaviorEntry<KillsTrackerBehavior> KILLS_TRACKER;
	public static final GameBehaviorEntry<TimeSurvivedTrackerBehavior> TIME_SURVIVED_TRACKER;
	public static final GameBehaviorEntry<DamageTrackerBehavior> DAMAGE_TRACKER;
	public static final GameBehaviorEntry<BlocksBrokenTrackerBehavior> BLOCKS_BROKEN_TRACKER;

	public static final GameBehaviorEntry<DisplayLeaderboardOnFinishBehavior<?>> DISPLAY_LEADERBOARD_ON_FINISH;

	public static final GameBehaviorEntry<DonationPackageBehavior> DONATION_PACKAGE;
	public static final GameBehaviorEntry<LootPackageBehavior> LOOT_PACKAGE;
	public static final GameBehaviorEntry<EffectPackageBehavior> EFFECT_PACKAGE;
	public static final GameBehaviorEntry<SwapPlayersPackageBehavior> SWAP_PLAYERS_PACKAGE;
	public static final GameBehaviorEntry<SpawnEntityAtPlayerPackageBehavior> SPAWN_ENTITY_AT_PLAYER_PACKAGE;
	public static final GameBehaviorEntry<SpawnEntityAtRegionsPackageBehavior> SPAWN_ENTITY_AT_REGIONS_PACKAGE;
	public static final GameBehaviorEntry<SpawnEntitiesAroundPlayersPackageBehavior> SPAWN_ENTITIES_AROUND_PLAYERS_PACKAGE;
	public static final GameBehaviorEntry<SpawnEntitiesAtRegionsOverTimePackageBehavior> SPAWN_ENTITIES_AT_REGIONS_OVER_TIME_PACKAGE;
	public static final GameBehaviorEntry<SetBlockAtPlayerPackageBehavior> SET_BLOCK_AT_PLAYER_PACKAGE;
	public static final GameBehaviorEntry<GivePlayerHeadPackageBehavior> GIVE_PLAYER_HEAD_PACKAGE;
	public static final GameBehaviorEntry<ShootProjectilesAroundPlayerPackageBehavior> SHOOT_PROJECTILES_AT_PLAYER;

	public static <T extends IGameBehavior> GameBehaviorEntry<T> register(final String name, final Codec<T> codec) {
		return REGISTRATE.object(name).behavior(codec).register();
	}

	static {
		POSITION_PLAYERS = register("position_players", PositionPlayersBehavior.CODEC);
		TIMED = register("timed", TimedGameBehavior.CODEC);
		RESPAWN_SPECTATOR = register("respawn_spectator", RespawnSpectatorBehavior.CODEC);
		COMMANDS = register("commands", CommandEventsBehavior.CODEC);
		ISOLATE_PLAYER_STATE = register("isolate_player_state", IsolatePlayerStateBehavior.CODEC);
		SET_GAME_TYPES = register("set_game_types", SetGameTypesBehavior.CODEC);
		PHASE_CONTROLLER = register("phase_controller", PhaseControllerBehavior.CODEC);
		SCHEDULED_MESSAGES = register("scheduled_messages", ScheduledMessagesBehavior.CODEC);
		FIREWORKS_ON_DEATH = register("fireworks_on_death", FireworksOnDeathBehavior.CODEC);
		BIND_CONTROLS = register("bind_controls", BindControlsBehavior.CODEC);
		CANCEL_PLAYER_DAMAGE = register("cancel_player_damage", CancelPlayerDamageBehavior.CODEC);
		SET_GAME_RULES = register("set_game_rules", SetGameRulesBehavior.CODEC);
		TEAMS = register("teams", TeamsBehavior.CODEC);
		SPECTATOR_CHASE = register("spectator_chase", SpectatorChaseBehavior.CODEC);
		FORCE_LOAD_REGION = register("force_load_region", ForceLoadRegionBehavior.CODEC);
		SET_BLOCKS = register("set_blocks", SetBlocksBehavior.CODEC);
		ELIMINATE_PLAYER_CONTROL = register("eliminate_player_control", EliminatePlayerControlBehavior.CODEC);
		FILL_CHESTS_BY_MARKER = register("fill_chests_by_marker", FillChestsByMarkerBehavior.CODEC);
		GENERATE_ENTITIES = register("generate_entities", GenerateEntitiesBehavior.CODEC);
		WEATHER_CONTROLS = register("weather_controls", WeatherControlsBehavior.CODEC);
		RUN_COMMAND_IN_REGION = register("run_command_in_region", RunCommandInRegionBehavior.CODEC);
		TNT_AUTO_FUSE = register("tnt_auto_fuse", TntAutoFuseBehavior.CODEC);
		DISABLE_HUNGER = register("disable_hunger", DisableHungerBehavior.CODEC);
		DISABLE_TNT_BLOCK_DESTRUCTION = register("disable_tnt_block_destruction", DisableTntDestructionBehavior.CODEC);
		SET_MAX_HEALTH = register("set_max_health", SetMaxHealthBehavior.CODEC);
		TEAM_WIN_TRIGGER = register("team_win_trigger", TeamWinTrigger.CODEC);
		INDIVIDUAL_WIN_TRIGGER = register("individual_win_trigger", IndividualWinTrigger.CODEC);

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

		DONATION_PACKAGE = register("donation_package", DonationPackageBehavior.CODEC);
		LOOT_PACKAGE = register("loot_package", LootPackageBehavior.CODEC);
		EFFECT_PACKAGE = register("effect_package", EffectPackageBehavior.CODEC);
		SWAP_PLAYERS_PACKAGE = register("swap_players_package", SwapPlayersPackageBehavior.CODEC);
		SPAWN_ENTITY_AT_PLAYER_PACKAGE = register("spawn_entity_at_player_package", SpawnEntityAtPlayerPackageBehavior.CODEC);
		SPAWN_ENTITY_AT_REGIONS_PACKAGE = register("spawn_entity_at_regions_package", SpawnEntityAtRegionsPackageBehavior.CODEC);
		SPAWN_ENTITIES_AROUND_PLAYERS_PACKAGE = register("spawn_entities_around_players_package", SpawnEntitiesAroundPlayersPackageBehavior.CODEC);
		SPAWN_ENTITIES_AT_REGIONS_OVER_TIME_PACKAGE = register("spawn_entities_at_regions_over_time_package", SpawnEntitiesAtRegionsOverTimePackageBehavior.CODEC);
		SET_BLOCK_AT_PLAYER_PACKAGE = register("set_block_at_player_package", SetBlockAtPlayerPackageBehavior.CODEC);
		GIVE_PLAYER_HEAD_PACKAGE = register("give_player_head_package", GivePlayerHeadPackageBehavior.CODEC);
		SHOOT_PROJECTILES_AT_PLAYER = register("shoot_projectiles_at_player", ShootProjectilesAroundPlayerPackageBehavior.CODEC);
	}

	public static void init(IEventBus modBus) {
		REGISTER.register(modBus);
	}
}
