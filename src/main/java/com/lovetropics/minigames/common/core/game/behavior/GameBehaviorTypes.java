package com.lovetropics.minigames.common.core.game.behavior;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.core.game.behavior.action.ApplyToBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.PlayerActionTarget;
import com.lovetropics.minigames.common.core.game.behavior.action.PlotActionTarget;
import com.lovetropics.minigames.common.core.game.behavior.instances.AddWeatherBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.ArmorParticipantsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.AssignPlayerRolesBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.CompositeBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.DebugModeBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.EquipParticipantsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.GameEndEffectsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.GameProgressionBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.ImmediateRespawnBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.IndividualWinTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.JoinLateWithRoleBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.PermanentItemBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.PositionPlayersBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.ProgressBarBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.SetGameClientStateBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.SetGameTypesBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.SetupIntegrationsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.SpectatorChaseBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.TimedGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.TipsAndTricksBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.ApplyGlobalDisguiseAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.ChestDropAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.CountdownAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.DamagePlayerAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.EliminatePlayerAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.GiveEffectAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.GiveLootAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.NotificationToastAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.PlaySoundAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.RunCommandsAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SendMessageAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SetBlockAtPlayerAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SetBlocksAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SetExtendingBlocksAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.ShootProjectilesAroundPlayerAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.ShowTitleAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SpawnEntitiesAroundPlayersAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SpawnEntitiesAtRegionsOverTimeAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SpawnEntityAtPlayerAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SpawnEntityAtRegionsAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SpawnFireworksAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SpawnParticlesAroundPlayerAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SpawnTornadoAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SpectatorActivityAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SwapPlayersAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.TargetPlayerAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.TransformPlayerTornadoAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.WeatherEventAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.command.WeatherControlsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.BlockPackagesDuringPhaseBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.DonationPackageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.DonationThresholdBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.GivePlayerHeadPackageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.BindObjectiveToStatisticBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.BlocksBrokenTrackerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.CampingTrackerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.CauseOfDeathTrackerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.DamageTrackerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.DisplayLeaderboardOnFinishBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.KillsTrackerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.PlaceByDeathOrderBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.PlaceByStatisticBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.TimeSurvivedTrackerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.SetupTeamsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.SyncTeamsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.TeamChatBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.TeamWinTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.TeamsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.BindControlsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.GeneralEventsTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.OnDamageTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.OnDeathTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.OnKillTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.PhaseChangeTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.ScheduledActionsTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.WeatherChangeTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.WhileInRegionTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.phase.GameReadyTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.phase.GameTickTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.phase.StartGameTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.phase.StopGameTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.CancelPlayerDamageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.DisableHungerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.DisableTntDestructionBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.ScaleDamageFromEntityBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.ScaleExplosionKnockbackBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.ScalePlayerDamageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.SetDayTimeBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.SetDifficultyBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.SetGameRulesBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.SetMaxHealthBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.SetTimeSpeedBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.TntAutoFuseBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.world.FillChestsByMarkerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.world.ForceLoadRegionBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.world.GenerateEntitiesBehavior;
import com.lovetropics.minigames.common.util.registry.GameBehaviorEntry;
import com.lovetropics.minigames.common.util.registry.LoveTropicsRegistrate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class GameBehaviorTypes {
	public static final ResourceKey<Registry<GameBehaviorType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(new ResourceLocation(Constants.MODID, "minigame_behaviours"));
	public static final DeferredRegister<GameBehaviorType<?>> REGISTER = DeferredRegister.create(REGISTRY_KEY, Constants.MODID);

	public static final Supplier<IForgeRegistry<GameBehaviorType<?>>> REGISTRY = REGISTER.makeRegistry(() -> new RegistryBuilder<GameBehaviorType<?>>()
			.disableSync()
			.disableSaving());

	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final Codec<GameBehaviorType<?>> TYPE_CODEC = ExtraCodecs.lazyInitializedCodec(() -> REGISTRY.get().getCodec());

	public static final GameBehaviorEntry<CompositeBehavior> COMPOSITE = register("composite", CompositeBehavior.MAP_CODEC);
	public static final GameBehaviorEntry<PositionPlayersBehavior> POSITION_PLAYERS = register("position_players", PositionPlayersBehavior.CODEC);
	public static final GameBehaviorEntry<TimedGameBehavior> TIMED = register("timed", TimedGameBehavior.CODEC);
	public static final GameBehaviorEntry<ImmediateRespawnBehavior> IMMEDIATE_RESPAWN = register("immediate_respawn", ImmediateRespawnBehavior.CODEC);
	public static final GameBehaviorEntry<SetGameTypesBehavior> SET_GAME_TYPES = register("set_game_types", SetGameTypesBehavior.CODEC);
	public static final GameBehaviorEntry<GameProgressionBehavior> PROGRESSION = register("progression", GameProgressionBehavior.CODEC);
	public static final GameBehaviorEntry<PermanentItemBehavior> PERMANENT_ITEM = register("permanent_item", PermanentItemBehavior.CODEC);
	public static final GameBehaviorEntry<GeneralEventsTrigger> EVENTS = register("events", GeneralEventsTrigger.CODEC);

	public static final GameBehaviorEntry<StopGameTrigger> STOP_GAME = register("phase_triggers/stop", StopGameTrigger.CODEC);
	public static final GameBehaviorEntry<StartGameTrigger> START_GAME = register("phase_triggers/start", StartGameTrigger.CODEC);
	public static final GameBehaviorEntry<GameReadyTrigger> GAME_READY = register("phase_triggers/ready", GameReadyTrigger.CODEC);
	public static final GameBehaviorEntry<GameTickTrigger> GAME_TICK = register("events/game/tick", GameTickTrigger.CODEC);

	public static final GameBehaviorEntry<OnDeathTrigger> ON_DEATH = register("on_death", OnDeathTrigger.CODEC);
	public static final GameBehaviorEntry<OnDamageTrigger> ON_DAMAGE = register("on_damage", OnDamageTrigger.CODEC);
	public static final GameBehaviorEntry<WhileInRegionTrigger> WHILE_IN_REGION = register("while_in_region", WhileInRegionTrigger.CODEC);
	public static final GameBehaviorEntry<ScheduledActionsTrigger> SCHEDULED_ACTIONS = register("scheduled_actions", ScheduledActionsTrigger.CODEC);
	public static final GameBehaviorEntry<PhaseChangeTrigger> PHASE_CHANGE = register("phase_change", PhaseChangeTrigger.CODEC);
	public static final GameBehaviorEntry<OnKillTrigger> ON_KILL = register("on_kill", OnKillTrigger.CODEC);
	public static final GameBehaviorEntry<BindControlsBehavior> BIND_CONTROLS = register("bind_controls", BindControlsBehavior.CODEC);
	public static final GameBehaviorEntry<CancelPlayerDamageBehavior> CANCEL_PLAYER_DAMAGE = register("cancel_player_damage", CancelPlayerDamageBehavior.CODEC);
	public static final GameBehaviorEntry<ScalePlayerDamageBehavior> SCALE_PLAYER_DAMAGE = register("scale_player_damage", ScalePlayerDamageBehavior.CODEC);
	public static final GameBehaviorEntry<ScaleDamageFromEntityBehavior> SCALE_DAMAGE_FROM_ENTITY = register("scale_damage_from_entity", ScaleDamageFromEntityBehavior.CODEC);
	public static final GameBehaviorEntry<ScaleExplosionKnockbackBehavior> SCALE_EXPLOSION_KNOCKBACK = register("scale_explosion_knockback", ScaleExplosionKnockbackBehavior.CODEC);
	public static final GameBehaviorEntry<SetGameRulesBehavior> SET_GAME_RULES = register("set_game_rules", SetGameRulesBehavior.CODEC);
	public static final GameBehaviorEntry<SetupTeamsBehavior> SETUP_TEAMS = register("setup_teams", SetupTeamsBehavior.CODEC);
	public static final GameBehaviorEntry<TeamsBehavior> TEAMS = register("teams", TeamsBehavior.CODEC);
	public static final GameBehaviorEntry<TeamChatBehavior> TEAM_CHAT = register("team_chat", TeamChatBehavior.CODEC);
	public static final GameBehaviorEntry<SpectatorChaseBehavior> SPECTATOR_CHASE = register("spectator_chase", SpectatorChaseBehavior.CODEC);
	public static final GameBehaviorEntry<ForceLoadRegionBehavior> FORCE_LOAD_REGION = register("force_load_region", ForceLoadRegionBehavior.CODEC);
	public static final GameBehaviorEntry<EliminatePlayerAction> ELIMINATE_PLAYER = register("eliminate_player", EliminatePlayerAction.CODEC);
	public static final GameBehaviorEntry<FillChestsByMarkerBehavior> FILL_CHESTS_BY_MARKER = register("fill_chests_by_marker", FillChestsByMarkerBehavior.CODEC);
	public static final GameBehaviorEntry<GenerateEntitiesBehavior> GENERATE_ENTITIES = register("generate_entities", GenerateEntitiesBehavior.CODEC);
	public static final GameBehaviorEntry<AddWeatherBehavior> ADD_WEATHER = register("add_weather", AddWeatherBehavior.CODEC);
	public static final GameBehaviorEntry<WeatherControlsBehavior> WEATHER_CONTROLS = register("weather_controls", WeatherControlsBehavior.CODEC);
	public static final GameBehaviorEntry<TntAutoFuseBehavior> TNT_AUTO_FUSE = register("tnt_auto_fuse", TntAutoFuseBehavior.CODEC);
	public static final GameBehaviorEntry<DisableHungerBehavior> DISABLE_HUNGER = register("disable_hunger", DisableHungerBehavior.CODEC);
	public static final GameBehaviorEntry<DisableTntDestructionBehavior> DISABLE_TNT_BLOCK_DESTRUCTION = register("disable_tnt_block_destruction", DisableTntDestructionBehavior.CODEC);
	public static final GameBehaviorEntry<SetMaxHealthBehavior> SET_MAX_HEALTH = register("set_max_health", SetMaxHealthBehavior.CODEC);
	public static final GameBehaviorEntry<IndividualWinTrigger> INDIVIDUAL_WIN_TRIGGER = register("individual_win_trigger", IndividualWinTrigger.CODEC);
	public static final GameBehaviorEntry<TeamWinTrigger> TEAM_WIN_TRIGGER = register("team_win_trigger", TeamWinTrigger.CODEC);
	public static final GameBehaviorEntry<SyncTeamsBehavior> SYNC_TEAMS = register("sync_teams", SyncTeamsBehavior.CODEC);
	public static final GameBehaviorEntry<EquipParticipantsBehavior> EQUIP_PARTICIPANTS = register("equip_participants", EquipParticipantsBehavior.CODEC);
	public static final GameBehaviorEntry<ArmorParticipantsBehavior> ARMOR_PARTICIPANTS = register("armor_participants", ArmorParticipantsBehavior.CODEC);
	public static final GameBehaviorEntry<SetTimeSpeedBehavior> SET_TIME_SPEED = register("set_time_speed", SetTimeSpeedBehavior.CODEC);
	public static final GameBehaviorEntry<SetDayTimeBehavior> SET_DAY_TIME = register("set_day_time", SetDayTimeBehavior.CODEC);
	public static final GameBehaviorEntry<SetDifficultyBehavior> SET_DIFFICULTY = register("set_difficulty", SetDifficultyBehavior.CODEC);
	public static final GameBehaviorEntry<GameEndEffectsBehavior> GAME_END_EFFECTS = register("game_end_effects", GameEndEffectsBehavior.CODEC);
	public static final GameBehaviorEntry<TipsAndTricksBehavior> TIPS_AND_TRICKS = register("tips_and_tricks", TipsAndTricksBehavior.CODEC);
	public static final GameBehaviorEntry<ProgressBarBehavior> PHASE_PROGRESS_BAR = register("phase_progress_bar", ProgressBarBehavior.CODEC);
	public static final GameBehaviorEntry<WeatherChangeTrigger> WEATHER_CHANGE_TRIGGER = register("weather_change_trigger", WeatherChangeTrigger.CODEC);

	public static final GameBehaviorEntry<BindObjectiveToStatisticBehavior> BIND_OBJECTIVE_TO_STATISTIC = register("bind_objective_to_statistic", BindObjectiveToStatisticBehavior.CODEC);
	public static final GameBehaviorEntry<PlaceByStatisticBehavior> PLACE_BY_STATISTIC = register("place_by_statistic", PlaceByStatisticBehavior.CODEC);
	public static final GameBehaviorEntry<PlaceByDeathOrderBehavior> PLACE_BY_DEATH_ORDER = register("place_by_death_order", PlaceByDeathOrderBehavior.CODEC);
	public static final GameBehaviorEntry<CampingTrackerBehavior> CAMPING_TRACKER = register("camping_tracker", CampingTrackerBehavior.CODEC);
	public static final GameBehaviorEntry<CauseOfDeathTrackerBehavior> CAUSE_OF_DEATH_TRACKER = register("cause_of_death_tracker", CauseOfDeathTrackerBehavior.CODEC);
	public static final GameBehaviorEntry<KillsTrackerBehavior> KILLS_TRACKER = register("kills_tracker", KillsTrackerBehavior.CODEC);
	public static final GameBehaviorEntry<TimeSurvivedTrackerBehavior> TIME_SURVIVED_TRACKER = register("time_survived_tracker", TimeSurvivedTrackerBehavior.CODEC);
	public static final GameBehaviorEntry<DamageTrackerBehavior> DAMAGE_TRACKER = register("damage_tracker", DamageTrackerBehavior.CODEC);
	public static final GameBehaviorEntry<BlocksBrokenTrackerBehavior> BLOCKS_BROKEN_TRACKER = register("blocks_broken_tracker", BlocksBrokenTrackerBehavior.CODEC);

	public static final GameBehaviorEntry<DisplayLeaderboardOnFinishBehavior<?>> DISPLAY_LEADERBOARD_ON_FINISH = register("display_leaderboard_on_finish", DisplayLeaderboardOnFinishBehavior.CODEC);

	public static final GameBehaviorEntry<DonationPackageBehavior> DONATION_PACKAGE = register("donation_package", DonationPackageBehavior.CODEC);
	public static final GameBehaviorEntry<DonationThresholdBehavior> DONATION_THRESHOLD = register("donation_threshold", DonationThresholdBehavior.CODEC);
	public static final GameBehaviorEntry<GiveLootAction> GIVE_LOOT = register("give_loot", GiveLootAction.CODEC);
	public static final GameBehaviorEntry<GiveEffectAction> GIVE_EFFECT = register("give_effect", GiveEffectAction.CODEC);
	public static final GameBehaviorEntry<SwapPlayersAction> SWAP_PLAYERS = register("swap_players", SwapPlayersAction.CODEC);
	public static final GameBehaviorEntry<SpawnEntityAtPlayerAction> SPAWN_ENTITY_AT_PLAYER = register("spawn_entity_at_player", SpawnEntityAtPlayerAction.CODEC);
	public static final GameBehaviorEntry<SpawnEntityAtRegionsAction> SPAWN_ENTITY_AT_REGIONS = register("spawn_entity_at_regions", SpawnEntityAtRegionsAction.CODEC);
	public static final GameBehaviorEntry<SpawnEntitiesAroundPlayersAction> SPAWN_ENTITIES_AROUND_PLAYERS = register("spawn_entities_around_players", SpawnEntitiesAroundPlayersAction.CODEC);
	public static final GameBehaviorEntry<SpawnEntitiesAtRegionsOverTimeAction> SPAWN_ENTITIES_AT_REGIONS_OVER_TIME = register("spawn_entities_at_regions_over_time", SpawnEntitiesAtRegionsOverTimeAction.CODEC);
	public static final GameBehaviorEntry<SetBlocksAction> SET_BLOCKS = register("set_blocks", SetBlocksAction.CODEC);
	public static final GameBehaviorEntry<SetExtendingBlocksAction> SET_EXTENDING_BLOCKS = register("set_extending_blocks", SetExtendingBlocksAction.CODEC);
	public static final GameBehaviorEntry<SetBlockAtPlayerAction> SET_BLOCK_AT_PLAYER = register("set_block_at_player", SetBlockAtPlayerAction.CODEC);
	public static final GameBehaviorEntry<GivePlayerHeadPackageBehavior> GIVE_PLAYER_HEAD_PACKAGE = register("give_player_head_package", GivePlayerHeadPackageBehavior.CODEC);
	public static final GameBehaviorEntry<ShootProjectilesAroundPlayerAction> SHOOT_PROJECTILES_AT_PLAYER = register("shoot_projectiles_at_player", ShootProjectilesAroundPlayerAction.CODEC);
	public static final GameBehaviorEntry<ApplyGlobalDisguiseAction> APPLY_GLOBAL_DISGUISE = register("apply_global_disguise", ApplyGlobalDisguiseAction.CODEC);
	public static final GameBehaviorEntry<BlockPackagesDuringPhaseBehavior> BLOCK_PACKAGES_DURING_PHASE = register("block_packages_during_phase", BlockPackagesDuringPhaseBehavior.CODEC);
	public static final GameBehaviorEntry<WeatherEventAction> WEATHER_EVENT = register("weather_event", WeatherEventAction.CODEC);
	public static final GameBehaviorEntry<CountdownAction<?>> COUNTDOWN_ACTION = register("countdown_action", CountdownAction.CODEC);
	public static final GameBehaviorEntry<TargetPlayerAction> TARGET_PLAYER = register("target_player", TargetPlayerAction.CODEC);
	public static final GameBehaviorEntry<SpawnFireworksAction> SPAWN_FIREWORKS = register("spawn_fireworks", SpawnFireworksAction.CODEC);
	public static final GameBehaviorEntry<RunCommandsAction> RUN_COMMANDS = register("run_commands", RunCommandsAction.CODEC);
	public static final GameBehaviorEntry<SendMessageAction> SEND_MESSAGE = register("send_message", SendMessageAction.CODEC);
	public static final GameBehaviorEntry<ShowTitleAction> SHOW_TITLE = register("show_title", ShowTitleAction.CODEC);
	public static final GameBehaviorEntry<PlaySoundAction> PLAY_SOUND = register("play_sound", PlaySoundAction.CODEC);
	public static final GameBehaviorEntry<SpawnParticlesAroundPlayerAction> SPAWN_PARTICLES_AROUND_PLAYER = register("spawn_particles_around_player", SpawnParticlesAroundPlayerAction.CODEC);
	public static final GameBehaviorEntry<NotificationToastAction> NOTIFICATION_TOAST = register("notification_toast", NotificationToastAction.CODEC);
	public static final GameBehaviorEntry<TransformPlayerTornadoAction> TRANSFORM_PLAYER_TORNADO = register("transform_player_tornado", TransformPlayerTornadoAction.CODEC);
	public static final GameBehaviorEntry<SpawnTornadoAction> SPAWN_TORNADO = register("spawn_tornado", SpawnTornadoAction.CODEC);
	public static final GameBehaviorEntry<ChestDropAction> CHEST_DROP = register("chest_drop", ChestDropAction.CODEC);
	public static final GameBehaviorEntry<DamagePlayerAction> DAMAGE_PLAYER = register("damage_player", DamagePlayerAction.CODEC);
	public static final GameBehaviorEntry<SpectatorActivityAction> SPECTATOR_ACTIVITY = register("spectator_activity", SpectatorActivityAction.CODEC);

	public static final GameBehaviorEntry<SetupIntegrationsBehavior> SETUP_INTEGRATIONS = register("setup_integrations", SetupIntegrationsBehavior.CODEC);
	public static final GameBehaviorEntry<AssignPlayerRolesBehavior> ASSIGN_PLAYER_ROLES = register("assign_player_roles", AssignPlayerRolesBehavior.CODEC);
	public static final GameBehaviorEntry<JoinLateWithRoleBehavior> JOIN_LATE_WITH_ROLE = register("join_late_with_role", JoinLateWithRoleBehavior.CODEC);
	public static final GameBehaviorEntry<DebugModeBehavior> DEBUG_MODE = register("debug_mode", DebugModeBehavior.CODEC);

	public static final GameBehaviorEntry<SetGameClientStateBehavior> SET_CLIENT_STATE = register("set_client_state", SetGameClientStateBehavior.CODEC);

	public static final GameBehaviorEntry<ApplyToBehavior<Plot, PlotActionTarget>> APPLY_TO_PLOT = register("apply_to_plot", ApplyToBehavior.PLOT_CODEC);
	public static final GameBehaviorEntry<ApplyToBehavior<ServerPlayer, PlayerActionTarget>> APPLY_TO_PLAYER = register("apply_to_player", ApplyToBehavior.PLAYER_CODEC);

	public static <T extends IGameBehavior> GameBehaviorEntry<T> register(final String name, final MapCodec<T> codec) {
		return REGISTRATE.object(name).behavior(codec).register();
	}

	public static void init(IEventBus modBus) {
		REGISTER.register(modBus);
	}
}
