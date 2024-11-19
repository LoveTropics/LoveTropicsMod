package com.lovetropics.minigames.common.core.game.behavior;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.river_race.behaviour.KillInVoidBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.ApplyToBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.PlayerActionTarget;
import com.lovetropics.minigames.common.core.game.behavior.action.PlotActionTarget;
import com.lovetropics.minigames.common.core.game.behavior.instances.AddWeatherBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.AssignPlayerRolesBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.CheckpointsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.CompositeBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.CountdownEffectsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.DebugModeBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.FirstEmptyTeamWinTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.GameEndEffectsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.GameProgressionBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.GiveItemsToKillerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.ImmediateRespawnBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.LastRemainingWinTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.JoinLateWithRoleBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.RisingFluidBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.ScoreMobInGoalBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.OnlyTickInPeriodBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.PermanentItemBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.PlayerHeadRewardBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.PointsSidebarBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.PositionPlayersBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.ProgressBarBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.SetGameClientStateBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.SetGameTypesBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.SetupIntegrationsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.SpectatorChaseBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.TipsAndTricksBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.AddAttributeModifierAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.AddCollidersAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.AddEquipmentAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.AllowSingleFallAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.ApplyClientStateAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.ApplyForTimeAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.ChestDropAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.ClearAttributeModifierAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.ClearDisguiseAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.ClearEffectsAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.CloseGameAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.CountdownAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.DamagePlayerAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.KillEntitiesAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SetPlayerRoleAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.EndGameAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.ExtinguishPlayerFireAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.GiveEffectAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.GiveLootAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.GiveRewardAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.IncrementStatisticAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.NotificationToastAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.PlaySoundAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.RemoveClientStateAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.RemoveCollidersAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.ResetHungerAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.RunCommandsAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SendMessageAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SetBlockAtPlayerAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SetBlocksAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SetDisguiseAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SetExtendingBlocksAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SetGlowingAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SetStatisticAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SetTotalTimeAction;
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
import com.lovetropics.minigames.common.core.game.behavior.instances.action.StartProgressChannelAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.SwapPlayersAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.TargetPlayerAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.TransformPlayerTornadoAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.action.WeatherEventAction;
import com.lovetropics.minigames.common.core.game.behavior.instances.command.WeatherControlsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.BlockPackagesDuringPhaseBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.DonationPackageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.DonationThresholdBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.GivePlayerHeadPackageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.PackageCostModifierBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.TriggerEveryPackageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.BindObjectiveToStatisticBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.BlocksBrokenTrackerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.CampingTrackerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.CauseOfDeathTrackerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.DamageTrackerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.DisplayLeaderboardOnFinishBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.KillsTrackerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.PlaceByDeathOrderBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.PlaceByStatisticBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.SetWinnerStatisticBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.SetXpToStatisticBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.StatisticTagBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.SumStatisticBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.statistics.TimeSurvivedTrackerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.SetupTeamsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.SyncTeamsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.TeamChatBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.team.TeamsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.ApplyToPlayerWhileTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.ApplyToPlayersAround;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.BindControlsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.GameFinishTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.GeneralEventsTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.ItemPickedUpTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.OnDamageTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.OnDeathTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.PeriodicActionsTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.PhaseChangeTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.PlayerTickTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.ScheduledActionsTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.SetRoleTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.SpawnTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.TopPlayerTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.WeatherChangeTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.WhileInInventoryTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.WhileInRegionTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.phase.GameOverTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.phase.GameReadyTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.phase.GameTickTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.phase.StartGameTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.trigger.phase.StopGameTrigger;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.CancelPlayerAttacksBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.CancelPlayerDamageBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.DamageInWaterBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.DisableHungerBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.DisableThrowingItemsBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.DisableTntDestructionBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.PreventBreakBehavior;
import com.lovetropics.minigames.common.core.game.behavior.instances.tweak.ScaleDamageBehavior;
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
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GameBehaviorTypes {
	public static final ResourceKey<Registry<GameBehaviorType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(LoveTropics.location("minigame_behaviours"));
	public static final DeferredRegister<GameBehaviorType<?>> REGISTER = DeferredRegister.create(REGISTRY_KEY, LoveTropics.ID);

	public static final Registry<GameBehaviorType<?>> REGISTRY = REGISTER.makeRegistry(builder -> builder.sync(false));

	private static final LoveTropicsRegistrate REGISTRATE = LoveTropics.registrate();

	public static final Codec<GameBehaviorType<?>> TYPE_CODEC = Codec.lazyInitialized(() -> REGISTRY.byNameCodec());

	public static final GameBehaviorEntry<CompositeBehavior> COMPOSITE = register("composite", CompositeBehavior.MAP_CODEC);
	public static final GameBehaviorEntry<PositionPlayersBehavior> POSITION_PLAYERS = register("position_players", PositionPlayersBehavior.CODEC);
	public static final GameBehaviorEntry<ImmediateRespawnBehavior> IMMEDIATE_RESPAWN = register("immediate_respawn", ImmediateRespawnBehavior.CODEC);
	public static final GameBehaviorEntry<SetGameTypesBehavior> SET_GAME_TYPES = register("set_game_types", SetGameTypesBehavior.CODEC);
	public static final GameBehaviorEntry<GameProgressionBehavior> PROGRESSION = register("progression", GameProgressionBehavior.CODEC);
	public static final GameBehaviorEntry<PermanentItemBehavior> PERMANENT_ITEM = register("permanent_item", PermanentItemBehavior.CODEC);
	public static final GameBehaviorEntry<GeneralEventsTrigger> EVENTS = register("events", GeneralEventsTrigger.CODEC);

	public static final GameBehaviorEntry<StopGameTrigger> STOP_GAME = register("phase_triggers/stop", StopGameTrigger.CODEC);
	public static final GameBehaviorEntry<StartGameTrigger> START_GAME = register("phase_triggers/start", StartGameTrigger.CODEC);
	public static final GameBehaviorEntry<GameReadyTrigger> GAME_READY = register("phase_triggers/ready", GameReadyTrigger.CODEC);
	public static final GameBehaviorEntry<GameTickTrigger> GAME_TICK = register("events/game/tick", GameTickTrigger.CODEC);
	public static final GameBehaviorEntry<PlayerTickTrigger> PLAYER_TICK = register("events/player/tick", PlayerTickTrigger.CODEC);
	public static final GameBehaviorEntry<GameOverTrigger> GAME_OVER = register("events/game_over", GameOverTrigger.CODEC);

	public static final GameBehaviorEntry<OnDeathTrigger> ON_DEATH = register("on_death", OnDeathTrigger.CODEC);
	public static final GameBehaviorEntry<OnDamageTrigger> ON_DAMAGE = register("on_damage", OnDamageTrigger.CODEC);
	public static final GameBehaviorEntry<WhileInRegionTrigger> WHILE_IN_REGION = register("while_in_region", WhileInRegionTrigger.CODEC);
	public static final GameBehaviorEntry<ScheduledActionsTrigger> SCHEDULED_ACTIONS = register("scheduled_actions", ScheduledActionsTrigger.CODEC);
	public static final GameBehaviorEntry<PeriodicActionsTrigger> PERIODIC_ACTIONS = register("periodic_actions", PeriodicActionsTrigger.CODEC);
	public static final GameBehaviorEntry<PhaseChangeTrigger> PHASE_CHANGE = register("phase_change", PhaseChangeTrigger.CODEC);
	public static final GameBehaviorEntry<GameFinishTrigger> GAME_FINISH = register("game_finish", GameFinishTrigger.CODEC);
	public static final GameBehaviorEntry<BindControlsBehavior> BIND_CONTROLS = register("bind_controls", BindControlsBehavior.CODEC);
	public static final GameBehaviorEntry<CancelPlayerDamageBehavior> CANCEL_PLAYER_DAMAGE = register("cancel_player_damage", CancelPlayerDamageBehavior.CODEC);
	public static final GameBehaviorEntry<ScalePlayerDamageBehavior> SCALE_PLAYER_DAMAGE = register("scale_player_damage", ScalePlayerDamageBehavior.CODEC);
	public static final GameBehaviorEntry<ScaleDamageBehavior> SCALE_DAMAGE = register("scale_damage", ScaleDamageBehavior.CODEC);
	public static final GameBehaviorEntry<ScaleExplosionKnockbackBehavior> SCALE_EXPLOSION_KNOCKBACK = register("scale_explosion_knockback", ScaleExplosionKnockbackBehavior.CODEC);
	public static final GameBehaviorEntry<SetGameRulesBehavior> SET_GAME_RULES = register("set_game_rules", SetGameRulesBehavior.CODEC);
	public static final GameBehaviorEntry<SetupTeamsBehavior> SETUP_TEAMS = register("setup_teams", SetupTeamsBehavior.CODEC);
	public static final GameBehaviorEntry<TeamsBehavior> TEAMS = register("teams", TeamsBehavior.CODEC);
	public static final GameBehaviorEntry<TeamChatBehavior> TEAM_CHAT = register("team_chat", TeamChatBehavior.CODEC);
	public static final GameBehaviorEntry<SpectatorChaseBehavior> SPECTATOR_CHASE = register("spectator_chase", SpectatorChaseBehavior.CODEC);
	public static final GameBehaviorEntry<ForceLoadRegionBehavior> FORCE_LOAD_REGION = register("force_load_region", ForceLoadRegionBehavior.CODEC);
	public static final GameBehaviorEntry<SetPlayerRoleAction> SET_PLAYER_ROLE = register("set_player_role", SetPlayerRoleAction.CODEC);
	public static final GameBehaviorEntry<FillChestsByMarkerBehavior> FILL_CHESTS_BY_MARKER = register("fill_chests_by_marker", FillChestsByMarkerBehavior.CODEC);
	public static final GameBehaviorEntry<GenerateEntitiesBehavior> GENERATE_ENTITIES = register("generate_entities", GenerateEntitiesBehavior.CODEC);
	public static final GameBehaviorEntry<AddWeatherBehavior> ADD_WEATHER = register("add_weather", AddWeatherBehavior.CODEC);
	public static final GameBehaviorEntry<WeatherControlsBehavior> WEATHER_CONTROLS = register("weather_controls", WeatherControlsBehavior.CODEC);
	public static final GameBehaviorEntry<TntAutoFuseBehavior> TNT_AUTO_FUSE = register("tnt_auto_fuse", TntAutoFuseBehavior.CODEC);
	public static final GameBehaviorEntry<DisableHungerBehavior> DISABLE_HUNGER = register("disable_hunger", DisableHungerBehavior.CODEC);
	public static final GameBehaviorEntry<DisableThrowingItemsBehavior> DISABLE_THROWING_ITEMS = register("disable_throwing_items", DisableThrowingItemsBehavior.CODEC);
	public static final GameBehaviorEntry<DisableTntDestructionBehavior> DISABLE_TNT_BLOCK_DESTRUCTION = register("disable_tnt_block_destruction", DisableTntDestructionBehavior.CODEC);
	public static final GameBehaviorEntry<SetMaxHealthBehavior> SET_MAX_HEALTH = register("set_max_health", SetMaxHealthBehavior.CODEC);
	public static final GameBehaviorEntry<LastRemainingWinTrigger> LAST_REMAINING_WIN_TRIGGER = register("last_remaining_win_trigger", LastRemainingWinTrigger.CODEC);
	public static final GameBehaviorEntry<FirstEmptyTeamWinTrigger> FIRST_EMPTY_TEAM_WIN_TRIGGER = register("first_empty_team_win_trigger", FirstEmptyTeamWinTrigger.CODEC);
	public static final GameBehaviorEntry<SyncTeamsBehavior> SYNC_TEAMS = register("sync_teams", SyncTeamsBehavior.CODEC);
	public static final GameBehaviorEntry<AddEquipmentAction> ADD_EQUIPMENT = register("add_equipment", AddEquipmentAction.CODEC);
	public static final GameBehaviorEntry<ResetHungerAction> RESET_HUNGER = register("reset_hunger", ResetHungerAction.CODEC);
	public static final GameBehaviorEntry<ClearEffectsAction> CLEAR_EFFECTS = register("clear_effects", ClearEffectsAction.CODEC);
	public static final GameBehaviorEntry<ExtinguishPlayerFireAction> EXTINGUISH_PLAYER_ACTION = register("extinguish_player", ExtinguishPlayerFireAction.CODEC);
	public static final GameBehaviorEntry<SetTimeSpeedBehavior> SET_TIME_SPEED = register("set_time_speed", SetTimeSpeedBehavior.CODEC);
	public static final GameBehaviorEntry<SetDayTimeBehavior> SET_DAY_TIME = register("set_day_time", SetDayTimeBehavior.CODEC);
	public static final GameBehaviorEntry<SetDifficultyBehavior> SET_DIFFICULTY = register("set_difficulty", SetDifficultyBehavior.CODEC);
	public static final GameBehaviorEntry<GameEndEffectsBehavior> GAME_END_EFFECTS = register("game_end_effects", GameEndEffectsBehavior.CODEC);
	public static final GameBehaviorEntry<TipsAndTricksBehavior> TIPS_AND_TRICKS = register("tips_and_tricks", TipsAndTricksBehavior.CODEC);
	public static final GameBehaviorEntry<ProgressBarBehavior> PHASE_PROGRESS_BAR = register("phase_progress_bar", ProgressBarBehavior.CODEC);
	public static final GameBehaviorEntry<WeatherChangeTrigger> WEATHER_CHANGE_TRIGGER = register("weather_change_trigger", WeatherChangeTrigger.CODEC);
	public static final GameBehaviorEntry<PlayerHeadRewardBehavior> PLAYER_HEAD_REWARD = register("player_head_reward", PlayerHeadRewardBehavior.CODEC);
	public static final GameBehaviorEntry<PointsSidebarBehavior> POINTS_SIDEBAR = register("points_sidebar", PointsSidebarBehavior.CODEC);
	public static final GameBehaviorEntry<DamageInWaterBehavior> DAMAGE_IN_WATER = register("damage_in_water", DamageInWaterBehavior.CODEC);
	public static final GameBehaviorEntry<ApplyToPlayerWhileTrigger> APPLY_TO_PLAYER_WHILE = register("apply_to_player_while", ApplyToPlayerWhileTrigger.CODEC);
	public static final GameBehaviorEntry<ApplyToPlayersAround> APPLY_TO_PLAYERS_AROUND = register("apply_to_players_around", ApplyToPlayersAround.CODEC);

	public static final GameBehaviorEntry<BindObjectiveToStatisticBehavior> BIND_OBJECTIVE_TO_STATISTIC = register("bind_objective_to_statistic", BindObjectiveToStatisticBehavior.CODEC);
	public static final GameBehaviorEntry<PlaceByStatisticBehavior> PLACE_BY_STATISTIC = register("place_by_statistic", PlaceByStatisticBehavior.CODEC);
	public static final GameBehaviorEntry<PlaceByDeathOrderBehavior> PLACE_BY_DEATH_ORDER = register("place_by_death_order", PlaceByDeathOrderBehavior.CODEC);
	public static final GameBehaviorEntry<CampingTrackerBehavior> CAMPING_TRACKER = register("camping_tracker", CampingTrackerBehavior.CODEC);
	public static final GameBehaviorEntry<CauseOfDeathTrackerBehavior> CAUSE_OF_DEATH_TRACKER = register("cause_of_death_tracker", CauseOfDeathTrackerBehavior.CODEC);
	public static final GameBehaviorEntry<KillsTrackerBehavior> KILLS_TRACKER = register("kills_tracker", KillsTrackerBehavior.CODEC);
	public static final GameBehaviorEntry<TimeSurvivedTrackerBehavior> TIME_SURVIVED_TRACKER = register("time_survived_tracker", TimeSurvivedTrackerBehavior.CODEC);
	public static final GameBehaviorEntry<DamageTrackerBehavior> DAMAGE_TRACKER = register("damage_tracker", DamageTrackerBehavior.CODEC);
	public static final GameBehaviorEntry<BlocksBrokenTrackerBehavior> BLOCKS_BROKEN_TRACKER = register("blocks_broken_tracker", BlocksBrokenTrackerBehavior.CODEC);
	public static final GameBehaviorEntry<SumStatisticBehavior> SUM_STATISTIC = register("sum_statistic", SumStatisticBehavior.CODEC);
	public static final GameBehaviorEntry<StatisticTagBehavior> STATISTIC_TAG = register("statistic_tag", StatisticTagBehavior.CODEC);
	public static final GameBehaviorEntry<SetXpToStatisticBehavior> SET_XP_TO_STATISTIC = register("set_xp_to_statistic", SetXpToStatisticBehavior.CODEC);
	public static final GameBehaviorEntry<OnlyTickInPeriodBehavior> ONLY_TICK_IN_PERIOD = register("only_tick_in_period", OnlyTickInPeriodBehavior.CODEC);
	public static final GameBehaviorEntry<SetWinnerStatisticBehavior> SET_WINNER_STATISTIC = register("set_winner_statistic", SetWinnerStatisticBehavior.CODEC);

	public static final GameBehaviorEntry<DisplayLeaderboardOnFinishBehavior<?>> DISPLAY_LEADERBOARD_ON_FINISH = register("display_leaderboard_on_finish", DisplayLeaderboardOnFinishBehavior.CODEC);

	public static final GameBehaviorEntry<DonationPackageBehavior> DONATION_PACKAGE = register("donation_package", DonationPackageBehavior.CODEC);
	public static final GameBehaviorEntry<DonationThresholdBehavior> DONATION_THRESHOLD = register("donation_threshold", DonationThresholdBehavior.CODEC);
	public static final GameBehaviorEntry<GiveLootAction> GIVE_LOOT = register("give_loot", GiveLootAction.CODEC);
	public static final GameBehaviorEntry<GiveEffectAction> GIVE_EFFECT = register("give_effect", GiveEffectAction.CODEC);
	public static final GameBehaviorEntry<SetGlowingAction> SET_GLOWING = register("set_glowing", SetGlowingAction.CODEC);
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
	public static final GameBehaviorEntry<ApplyForTimeAction> APPLY_FOR_TIME = register("apply_for_time", ApplyForTimeAction.CODEC);
	public static final GameBehaviorEntry<SetDisguiseAction> SET_DISGUISE = register("set_disguise", SetDisguiseAction.CODEC);
	public static final GameBehaviorEntry<ClearDisguiseAction> CLEAR_DISGUISE = register("clear_disguise", ClearDisguiseAction.CODEC);
	public static final GameBehaviorEntry<AddAttributeModifierAction> ADD_ATTRIBUTE_MODIFIER = register("add_attribute_modifier", AddAttributeModifierAction.CODEC);
	public static final GameBehaviorEntry<ClearAttributeModifierAction> CLEAR_ATTRIBUTE_MODIFIER = register("clear_attribute_modifier", ClearAttributeModifierAction.CODEC);
	public static final GameBehaviorEntry<BlockPackagesDuringPhaseBehavior> BLOCK_PACKAGES_DURING_PHASE = register("block_packages_during_phase", BlockPackagesDuringPhaseBehavior.CODEC);
	public static final GameBehaviorEntry<PackageCostModifierBehavior> PACKAGE_COST_MODIFIER = register("package_cost_modifier", PackageCostModifierBehavior.CODEC);
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
	public static final GameBehaviorEntry<GiveRewardAction> GIVE_REWARD = register("give_reward", GiveRewardAction.CODEC);
	public static final GameBehaviorEntry<TopPlayerTrigger> TOP_PLAYER_TRIGGER = register("top_player_trigger", TopPlayerTrigger.CODEC);
	public static final GameBehaviorEntry<SetRoleTrigger> SET_ROLE = register("set_role", SetRoleTrigger.CODEC);
	public static final GameBehaviorEntry<SpawnTrigger> SPAWN = register("on_spawn", SpawnTrigger.CODEC);
	public static final GameBehaviorEntry<ItemPickedUpTrigger> ITEM_PICKED_UP = register("item_picked_up", ItemPickedUpTrigger.CODEC);
	public static final GameBehaviorEntry<GiveItemsToKillerBehavior> GIVE_ITEMS_TO_KILLER = register("give_items_to_killer", GiveItemsToKillerBehavior.CODEC);
	public static final GameBehaviorEntry<CountdownEffectsBehavior> COUNTDOWN_EFFECTS = register("countdown_effects", CountdownEffectsBehavior.CODEC);
	public static final GameBehaviorEntry<TriggerEveryPackageBehavior> TRIGGER_EVERY_PACKAGE = register("trigger_every_package", TriggerEveryPackageBehavior.CODEC);
	public static final GameBehaviorEntry<WhileInInventoryTrigger> WHILE_IN_INVENTORY = register("while_in_inventory", WhileInInventoryTrigger.CODEC);
	public static final GameBehaviorEntry<ScoreMobInGoalBehavior> SCORE_MOB_IN_GOAL = register("score_mob_in_goal", ScoreMobInGoalBehavior.CODEC);
	public static final GameBehaviorEntry<CancelPlayerAttacksBehavior> CANCEL_PLAYER_ATTACKS = register("cancel_player_attacks", CancelPlayerAttacksBehavior.CODEC);
	public static final GameBehaviorEntry<AddCollidersAction> ADD_COLLIDERS = register("add_colliders", AddCollidersAction.CODEC);
	public static final GameBehaviorEntry<RemoveCollidersAction> REMOVE_COLLIDERS = register("remove_colliders", RemoveCollidersAction.CODEC);
	public static final GameBehaviorEntry<IncrementStatisticAction> INCREMENT_STATISTIC = register("increment_statistic", IncrementStatisticAction.CODEC);
	public static final GameBehaviorEntry<SetStatisticAction> SET_STATISTIC = register("set_statistic", SetStatisticAction.CODEC);
	public static final GameBehaviorEntry<SetTotalTimeAction> SET_TOTAL_TIME = register("set_total_time", SetTotalTimeAction.CODEC);
	public static final GameBehaviorEntry<KillEntitiesAction> KILL_ENTITIES = register("kill_entities", KillEntitiesAction.CODEC);
	public static final GameBehaviorEntry<StartProgressChannelAction> START_PROGRESS_CHANNEL = register("start_progress_channel", StartProgressChannelAction.CODEC);
	public static final GameBehaviorEntry<PreventBreakBehavior> PREVENT_BREAK = register("prevent_break", PreventBreakBehavior.CODEC);
	public static final GameBehaviorEntry<RisingFluidBehavior> RISING_FLUID = register("rising_fluid", RisingFluidBehavior.CODEC);
	public static final GameBehaviorEntry<EndGameAction> END_GAME = register("end_game", EndGameAction.CODEC);
	public static final GameBehaviorEntry<CloseGameAction> CLOSE_GAME = register("close_game", CloseGameAction.CODEC);
	public static final GameBehaviorEntry<CheckpointsBehavior> CHECKPOINTS = register("checkpoints", CheckpointsBehavior.CODEC);
	public static final GameBehaviorEntry<KillInVoidBehavior> KILL_IN_VOID = register("kill_in_void", KillInVoidBehavior.CODEC);
	public static final GameBehaviorEntry<AllowSingleFallAction> ALLOW_SINGLE_FALL = register("allow_single_fall", AllowSingleFallAction.CODEC);

	public static final GameBehaviorEntry<SetupIntegrationsBehavior> SETUP_INTEGRATIONS = register("setup_integrations", SetupIntegrationsBehavior.CODEC);
	public static final GameBehaviorEntry<AssignPlayerRolesBehavior> ASSIGN_PLAYER_ROLES = register("assign_player_roles", AssignPlayerRolesBehavior.CODEC);
	public static final GameBehaviorEntry<JoinLateWithRoleBehavior> JOIN_LATE_WITH_ROLE = register("join_late_with_role", JoinLateWithRoleBehavior.CODEC);
	public static final GameBehaviorEntry<DebugModeBehavior> DEBUG_MODE = register("debug_mode", DebugModeBehavior.CODEC);

	public static final GameBehaviorEntry<SetGameClientStateBehavior> SET_CLIENT_STATE = register("set_client_state", SetGameClientStateBehavior.CODEC);
	public static final GameBehaviorEntry<ApplyClientStateAction> APPLY_CLIENT_STATE = register("apply_client_state", ApplyClientStateAction.CODEC);
	public static final GameBehaviorEntry<RemoveClientStateAction> REMOVE_CLIENT_STATE = register("remove_client_state", RemoveClientStateAction.CODEC);

	public static final GameBehaviorEntry<ApplyToBehavior<Plot, PlotActionTarget>> APPLY_TO_PLOT = register("apply_to_plot", ApplyToBehavior.PLOT_CODEC);
	public static final GameBehaviorEntry<ApplyToBehavior<ServerPlayer, PlayerActionTarget>> APPLY_TO_PLAYER = register("apply_to_player", ApplyToBehavior.PLAYER_CODEC);

	public static <T extends IGameBehavior> GameBehaviorEntry<T> register(final String name, final MapCodec<T> codec) {
		return REGISTRATE.object(name).behavior(codec).register();
	}

	public static void init(IEventBus modBus) {
		REGISTER.register(modBus);
	}
}
