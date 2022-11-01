package com.lovetropics.minigames.common.core.game.behavior;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.behavior.instances.*;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.*;
import com.lovetropics.minigames.common.core.game.behavior.instances.command.*;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.*;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.*;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.*;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.SetupTeamsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.TeamWinTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.TeamsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.*;
import com.lovetropics.minigames.common.core.game.behavior.instances.world.*;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
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
	public static final GameBehaviorEntry<ImmediateRespawnBehavior> IMMEDIATE_RESPAWN;
	public static final GameBehaviorEntry<SetGameTypesBehavior> SET_GAME_TYPES;
	public static final GameBehaviorEntry<PhaseControllerBehavior> PHASE_CONTROLLER;
	public static final GameBehaviorEntry<PermanentItemBehavior> PERMANENT_ITEM;
	public static final GameBehaviorEntry<GeneralEventsTrigger> EVENTS;
	public static final GameBehaviorEntry<OnDeathTrigger> ON_DEATH;
	public static final GameBehaviorEntry<WhileInRegionTrigger> WHILE_IN_REGION;
	public static final GameBehaviorEntry<ScheduledActionsTrigger> SCHEDULED_ACTIONS;
	public static final GameBehaviorEntry<PhaseChangeTrigger> PHASE_CHANGE;
	public static final GameBehaviorEntry<BindControlsBehavior> BIND_CONTROLS;
	public static final GameBehaviorEntry<CancelPlayerDamageBehavior> CANCEL_PLAYER_DAMAGE;
	public static final GameBehaviorEntry<SetGameRulesBehavior> SET_GAME_RULES;
	public static final GameBehaviorEntry<SetupTeamsBehavior> SETUP_TEAMS;
	public static final GameBehaviorEntry<TeamsBehavior> TEAMS;
	public static final GameBehaviorEntry<SpectatorChaseBehavior> SPECTATOR_CHASE;
	public static final GameBehaviorEntry<ForceLoadRegionBehavior> FORCE_LOAD_REGION;
	public static final GameBehaviorEntry<EliminatePlayerAction> ELIMINATE_PLAYER;
	public static final GameBehaviorEntry<FillChestsByMarkerBehavior> FILL_CHESTS_BY_MARKER;
	public static final GameBehaviorEntry<GenerateEntitiesBehavior> GENERATE_ENTITIES;
	public static final GameBehaviorEntry<AddWeatherBehavior> ADD_WEATHER;
	public static final GameBehaviorEntry<WeatherControlsBehavior> WEATHER_CONTROLS;
	public static final GameBehaviorEntry<TntAutoFuseBehavior> TNT_AUTO_FUSE;
	public static final GameBehaviorEntry<DisableHungerBehavior> DISABLE_HUNGER;
	public static final GameBehaviorEntry<DisableTntDestructionBehavior> DISABLE_TNT_BLOCK_DESTRUCTION;
	public static final GameBehaviorEntry<SetMaxHealthBehavior> SET_MAX_HEALTH;
	public static final GameBehaviorEntry<IndividualWinTrigger> INDIVIDUAL_WIN_TRIGGER;
	public static final GameBehaviorEntry<TeamWinTrigger> TEAM_WIN_TRIGGER;
	public static final GameBehaviorEntry<EquipParticipantsBehavior> EQUIP_PARTICIPANTS;
	public static final GameBehaviorEntry<ArmorParticipantsBehavior> ARMOR_PARTICIPANTS;
	public static final GameBehaviorEntry<SetTimeSpeedBehavior> SET_TIME_SPEED;
	public static final GameBehaviorEntry<SetDayTimeBehavior> SET_DAY_TIME;
	public static final GameBehaviorEntry<SetDifficultyBehavior> SET_DIFFICULTY;
	public static final GameBehaviorEntry<GameEndEffectsBehavior> GAME_END_EFFECTS;
	public static final GameBehaviorEntry<TipsAndTricksBehavior> TIPS_AND_TRICKS;
	public static final GameBehaviorEntry<PhaseProgressBarBehavior> PHASE_PROGRESS_BAR;

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
	public static final GameBehaviorEntry<GiveLootAction> GIVE_LOOT;
	public static final GameBehaviorEntry<GiveEffectAction> GIVE_EFFECT;
	public static final GameBehaviorEntry<SwapPlayersAction> SWAP_PLAYERS;
	public static final GameBehaviorEntry<SpawnEntityAtPlayerAction> SPAWN_ENTITY_AT_PLAYER;
	public static final GameBehaviorEntry<SpawnEntityAtRegionsAction> SPAWN_ENTITY_AT_REGIONS;
	public static final GameBehaviorEntry<SpawnEntitiesAroundPlayersAction> SPAWN_ENTITIES_AROUND_PLAYERS;
	public static final GameBehaviorEntry<SpawnEntitiesAtRegionsOverTimeAction> SPAWN_ENTITIES_AT_REGIONS_OVER_TIME;
	public static final GameBehaviorEntry<SetBlocksAction> SET_BLOCKS;
	public static final GameBehaviorEntry<SetExtendingBlocksAction> SET_EXTENDING_BLOCKS;
	public static final GameBehaviorEntry<SetBlockAtPlayerAction> SET_BLOCK_AT_PLAYER;
	public static final GameBehaviorEntry<GivePlayerHeadPackageBehavior> GIVE_PLAYER_HEAD_PACKAGE;
	public static final GameBehaviorEntry<ShootProjectilesAroundPlayerAction> SHOOT_PROJECTILES_AT_PLAYER;
	public static final GameBehaviorEntry<ApplyGlobalDisguiseAction> APPLY_GLOBAL_DISGUISE;
	public static final GameBehaviorEntry<BlockPackagesDuringPhaseBehavior> BLOCK_PACKAGES_DURING_PHASE;
	public static final GameBehaviorEntry<WeatherEventAction> WEATHER_EVENT;
	public static final GameBehaviorEntry<CountdownAction> COUNTDOWN_ACTION;
	public static final GameBehaviorEntry<SpawnFireworksAction> SPAWN_FIREWORKS;
	public static final GameBehaviorEntry<RunCommandsAction> RUN_COMMANDS;
	public static final GameBehaviorEntry<SendMessageAction> SEND_MESSAGE;
	public static final GameBehaviorEntry<ShowTitleAction> SHOW_TITLE;
	public static final GameBehaviorEntry<PlaySoundAction> PLAY_SOUND;
	public static final GameBehaviorEntry<SpawnParticlesAroundPlayerAction> SPAWN_PARTICLES_AROUND_PLAYER;
	public static final GameBehaviorEntry<NotificationToastAction> NOTIFICATION_TOAST;
	public static final GameBehaviorEntry<TransformPlayerTornadoAction> TRANSFORM_PLAYER_TORNADO;

	public static final GameBehaviorEntry<SetupTelemetryBehavior> SETUP_TELEMETRY;
	public static final GameBehaviorEntry<AssignPlayerRolesBehavior> ASSIGN_PLAYER_ROLES;
	public static final GameBehaviorEntry<JoinLateWithRoleBehavior> JOIN_LATE_WITH_ROLE;
	public static final GameBehaviorEntry<DebugModeBehavior> DEBUG_MODE;

	public static final GameBehaviorEntry<SetGameClientStateBehavior> SET_CLIENT_STATE;

	public static <T extends IGameBehavior> GameBehaviorEntry<T> register(final String name, final Codec<T> codec) {
		return REGISTRATE.object(name).behavior(codec).register();
	}

	static {
		POSITION_PLAYERS = register("position_players", PositionPlayersBehavior.CODEC);
		TIMED = register("timed", TimedGameBehavior.CODEC);
		IMMEDIATE_RESPAWN = register("immediate_respawn", ImmediateRespawnBehavior.CODEC);
		SET_GAME_TYPES = register("set_game_types", SetGameTypesBehavior.CODEC);
		PHASE_CONTROLLER = register("phase_controller", PhaseControllerBehavior.CODEC);
		PERMANENT_ITEM = register("permanent_item", PermanentItemBehavior.CODEC);
		BIND_CONTROLS = register("bind_controls", BindControlsBehavior.CODEC);
		CANCEL_PLAYER_DAMAGE = register("cancel_player_damage", CancelPlayerDamageBehavior.CODEC);
		SET_GAME_RULES = register("set_game_rules", SetGameRulesBehavior.CODEC);
		SETUP_TEAMS = register("setup_teams", SetupTeamsBehavior.CODEC);
		TEAMS = register("teams", TeamsBehavior.CODEC);
		SPECTATOR_CHASE = register("spectator_chase", SpectatorChaseBehavior.CODEC);
		FORCE_LOAD_REGION = register("force_load_region", ForceLoadRegionBehavior.CODEC);
		FILL_CHESTS_BY_MARKER = register("fill_chests_by_marker", FillChestsByMarkerBehavior.CODEC);
		GENERATE_ENTITIES = register("generate_entities", GenerateEntitiesBehavior.CODEC);
		ADD_WEATHER = register("add_weather", AddWeatherBehavior.CODEC);
		WEATHER_CONTROLS = register("weather_controls", WeatherControlsBehavior.CODEC);
		TNT_AUTO_FUSE = register("tnt_auto_fuse", TntAutoFuseBehavior.CODEC);
		DISABLE_HUNGER = register("disable_hunger", DisableHungerBehavior.CODEC);
		DISABLE_TNT_BLOCK_DESTRUCTION = register("disable_tnt_block_destruction", DisableTntDestructionBehavior.CODEC);
		SET_MAX_HEALTH = register("set_max_health", SetMaxHealthBehavior.CODEC);
		TEAM_WIN_TRIGGER = register("team_win_trigger", TeamWinTrigger.CODEC);
		INDIVIDUAL_WIN_TRIGGER = register("individual_win_trigger", IndividualWinTrigger.CODEC);
		EQUIP_PARTICIPANTS = register("equip_participants", EquipParticipantsBehavior.CODEC);
		ARMOR_PARTICIPANTS = register("armor_participants", ArmorParticipantsBehavior.CODEC);
		SET_TIME_SPEED = register("set_time_speed", SetTimeSpeedBehavior.CODEC);
		SET_DAY_TIME = register("set_day_time", SetDayTimeBehavior.CODEC);
		SET_DIFFICULTY = register("set_difficulty", SetDifficultyBehavior.CODEC);
		GAME_END_EFFECTS = register("game_end_effects", GameEndEffectsBehavior.CODEC);
		TIPS_AND_TRICKS = register("tips_and_tricks", TipsAndTricksBehavior.CODEC);
		PHASE_PROGRESS_BAR = register("phase_progress_bar", PhaseProgressBarBehavior.CODEC);

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

		EVENTS = register("events", GeneralEventsTrigger.CODEC);
		ON_DEATH = register("on_death", OnDeathTrigger.CODEC);
		WHILE_IN_REGION = register("while_in_region", WhileInRegionTrigger.CODEC);
		SCHEDULED_ACTIONS = register("scheduled_actions", ScheduledActionsTrigger.CODEC);
		PHASE_CHANGE = register("phase_change", PhaseChangeTrigger.CODEC);

		DONATION_PACKAGE = register("donation_package", DonationPackageBehavior.CODEC);
		GIVE_LOOT = register("give_loot", GiveLootAction.CODEC);
		GIVE_EFFECT = register("give_effect", GiveEffectAction.CODEC);
		SWAP_PLAYERS = register("swap_players", SwapPlayersAction.CODEC);
		SPAWN_ENTITY_AT_PLAYER = register("spawn_entity_at_player", SpawnEntityAtPlayerAction.CODEC);
		SPAWN_ENTITY_AT_REGIONS = register("spawn_entity_at_regions", SpawnEntityAtRegionsAction.CODEC);
		SPAWN_ENTITIES_AROUND_PLAYERS = register("spawn_entities_around_players", SpawnEntitiesAroundPlayersAction.CODEC);
		SPAWN_ENTITIES_AT_REGIONS_OVER_TIME = register("spawn_entities_at_regions_over_time", SpawnEntitiesAtRegionsOverTimeAction.CODEC);
		SET_BLOCKS = register("set_blocks", SetBlocksAction.CODEC);
		SET_EXTENDING_BLOCKS = register("set_extending_blocks", SetExtendingBlocksAction.CODEC);
		SET_BLOCK_AT_PLAYER = register("set_block_at_player", SetBlockAtPlayerAction.CODEC);
		GIVE_PLAYER_HEAD_PACKAGE = register("give_player_head_package", GivePlayerHeadPackageBehavior.CODEC);
		SHOOT_PROJECTILES_AT_PLAYER = register("shoot_projectiles_at_player", ShootProjectilesAroundPlayerAction.CODEC);
		APPLY_GLOBAL_DISGUISE = register("apply_global_disguise", ApplyGlobalDisguiseAction.CODEC);
		BLOCK_PACKAGES_DURING_PHASE = register("block_packages_during_phase", BlockPackagesDuringPhaseBehavior.CODEC);
		WEATHER_EVENT = register("weather_event", WeatherEventAction.CODEC);
		COUNTDOWN_ACTION = register("countdown_action", CountdownAction.CODEC);
		SPAWN_FIREWORKS = register("spawn_fireworks", SpawnFireworksAction.CODEC);
		RUN_COMMANDS = register("run_commands", RunCommandsAction.CODEC);
		SEND_MESSAGE = register("send_message", SendMessageAction.CODEC);
		ELIMINATE_PLAYER = register("eliminate_player", EliminatePlayerAction.CODEC);
		SHOW_TITLE = register("show_title", ShowTitleAction.CODEC);
		PLAY_SOUND = register("play_sound", PlaySoundAction.CODEC);
		SPAWN_PARTICLES_AROUND_PLAYER = register("spawn_particles_around_player", SpawnParticlesAroundPlayerAction.CODEC);
		NOTIFICATION_TOAST = register("notification_toast", NotificationToastAction.CODEC);
		TRANSFORM_PLAYER_TORNADO = register("transform_player_tornado", TransformPlayerTornadoAction.CODEC);

		SETUP_TELEMETRY = register("setup_telemetry", SetupTelemetryBehavior.CODEC);
		ASSIGN_PLAYER_ROLES = register("assign_player_roles", AssignPlayerRolesBehavior.CODEC);
		JOIN_LATE_WITH_ROLE = register("join_late_with_role", JoinLateWithRoleBehavior.CODEC);
		DEBUG_MODE = register("debug_mode", DebugModeBehavior.CODEC);

		SET_CLIENT_STATE = register("set_client_state", SetGameClientStateBehavior.CODEC);
	}

	public static void init(IEventBus modBus) {
		REGISTER.register(modBus);
	}
}
