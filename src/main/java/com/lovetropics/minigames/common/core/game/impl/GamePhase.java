package com.lovetropics.minigames.common.core.game.impl;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GamePhaseType;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGame;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.IGamePhaseDefinition;
import com.lovetropics.minigames.common.core.game.PlayerIsolation;
import com.lovetropics.minigames.common.core.game.SpawnBuilder;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorList;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.player.MutablePlayerSet;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.util.GameScheduler;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.core.map.MapRegions;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Think of a GamePhase like an act in a play, where the play is a GameInstance
 */
public class GamePhase implements IGamePhase {
	final GameInstance game;
	final MinecraftServer server;
	final IGamePhaseDefinition definition;
	final GamePhaseType phaseType;

	final GameMap map;
	final BehaviorList behaviors;
	final GameStateMap phaseState = new GameStateMap();

	final EnumMap<PlayerRole, MutablePlayerSet> roles = new EnumMap<>(PlayerRole.class);
	protected final Set<UUID> addedPlayers = new ObjectArraySet<>();

	final GameEventListeners events = new GameEventListeners();

	long startTime;
	@Nullable
	GameStopReason stopped;
	boolean destroyed;

	private final GameScheduler scheduler = new GameScheduler();

	protected GamePhase(GameInstance game, IGamePhaseDefinition definition, GamePhaseType phaseType, GameMap map, BehaviorList behaviors) {
		this.game = game;
		server = game.server();
		this.definition = definition;
		this.phaseType = phaseType;

		this.map = map;
		this.behaviors = behaviors;

		for (PlayerRole role : PlayerRole.ROLES) {
			MutablePlayerSet rolePlayers = new MutablePlayerSet(server);
			roles.put(role, rolePlayers);
		}
	}

	public static CompletableFuture<GameResult<GamePhase>> create(GameInstance game, IGamePhaseDefinition definition, GamePhaseType phaseType) {
		MinecraftServer server = game.server();

		GameResult<Unit> result = game.lobby.manager.canStartGamePhase(definition);
		if (result.isError()) {
			return CompletableFuture.completedFuture(result.castError());
		}

		CompletableFuture<GameResult<GamePhase>> future = definition.getMap().open(server)
				.thenApplyAsync(r -> r.map(map -> {
					BehaviorList behaviors = definition.createBehaviors();
					if(game.definition.isMultiGamePhase()){
						return new MultiGamePhase(game, definition, phaseType, map, behaviors);
					}
					return new GamePhase(game, definition, phaseType, map, behaviors);
				}), server);

		return GameResult.handleException("Unknown exception starting game phase", future);
	}
	public static CompletableFuture<GameResult<GamePhase>> createMultiGame(GameInstance game, IGamePhaseDefinition definition, GamePhaseType phaseType, ResourceLocation gameId) {
		MinecraftServer server = game.server();

		GameResult<Unit> result = game.lobby.manager.canStartGamePhase(definition);
		if (result.isError()) {
			return CompletableFuture.completedFuture(result.castError());
		}

		CompletableFuture<GameResult<GamePhase>> future = definition.getMap().open(server)
				.thenApplyAsync(r -> r.map(map -> {
					BehaviorList behaviors = definition.createBehaviors();
					if(game.definition.isMultiGamePhase()){
						return new MultiGamePhase(game, definition, phaseType, map, behaviors, gameId);
					}
					return new GamePhase(game, definition, phaseType, map, behaviors);
				}), server);

		return GameResult.handleException("Unknown exception starting game phase", future);
	}

	GameResult<Unit> start(final boolean savePlayerDataToMemory) {
		try {
			behaviors.registerTo(this, events);
		} catch (GameException e) {
			return GameResult.error(e.getTextMessage());
		}

		final String mapName = map.name();
		if (mapName != null) {
			statistics().global().set(StatisticKey.MAP, mapName);
		}

		startTime = level().getGameTime();

		try {
			invoker(GamePhaseEvents.CREATE).start();

			List<ServerPlayer> shuffledPlayers = Lists.newArrayList(allPlayers());
			Collections.shuffle(shuffledPlayers);

			for (ServerPlayer player : shuffledPlayers) {
				addAndSpawnPlayer(player, getRoleFor(player), savePlayerDataToMemory);
			}

			invoker(GamePhaseEvents.START).start();
		} catch (Exception e) {
			return GameResult.fromException("Failed to start game", e);
		}

		return GameResult.ok();
	}

	protected ServerPlayer addAndSpawnPlayer(ServerPlayer player, @Nullable PlayerRole role, final boolean savePlayerDataToMemory) {
		SpawnBuilder spawn = new SpawnBuilder(player);
		invoker(GamePlayerEvents.SPAWN).onSpawn(player.getUUID(), spawn, role);

		ServerPlayer newPlayer = PlayerIsolation.INSTANCE.teleportTo(player, spawn.level(), spawn.position(), spawn.yRot(), spawn.xRot());
		invoker(GamePlayerEvents.ADD).onAdd(newPlayer);
		spawn.applyInitializers(newPlayer);

		invoker(GamePlayerEvents.SET_ROLE).onSetRole(newPlayer, role, null);

		addedPlayers.add(player.getUUID());

		if (savePlayerDataToMemory) {
			game.playerStorage.setPlayerData(player, player.saveWithoutId(new CompoundTag()));
		}

		return newPlayer;
	}

	@Nullable
	GameStopReason tick() {
		try {
			scheduler.tick();
			invoker(GamePhaseEvents.TICK).tick();
		} catch (Exception e) {
			cancelWithError(e);
		}
		return stopped;
	}

	@Override
	public IGame game() {
		return game;
	}

	@Override
	public GameStateMap state() {
		return phaseState;
	}

	@Override
	public GamePhaseType phaseType() {
		return phaseType;
	}

	@Override
	public IGamePhaseDefinition phaseDefinition() {
		return definition;
	}

	@Override
	public GameEventListeners events() {
		return events;
	}

	@Override
	public <T> T invoker(GameEventType<T> type) {
		return events.invoker(type);
	}

	@Override
	public boolean setPlayerRole(ServerPlayer player, @Nullable PlayerRole role) {
		PlayerRole lastRole = getRoleFor(player);
		if (role == lastRole) return false;

		if (lastRole != null) {
			roles.get(lastRole).remove(player);
		}
		if (role != null) {
			roles.get(role).add(player);
		}

		// If we haven't added this player yet, just track state for now
		if (addedPlayers.contains(player.getUUID())) {
			onSetPlayerRole(player, role, lastRole);
		}

		return true;
	}

	private void onSetPlayerRole(ServerPlayer player, @Nullable PlayerRole role, @Nullable PlayerRole lastRole) {
		try {
			SpawnBuilder spawn = new SpawnBuilder(player);
			invoker(GamePlayerEvents.SPAWN).onSpawn(player.getUUID(), spawn, role);
			spawn.teleportAndApply(player);
			if (role != lastRole) {
				invoker(GamePlayerEvents.SET_ROLE).onSetRole(player, role, lastRole);
			}
		} catch (Exception e) {
			LoveTropics.LOGGER.warn("Failed to dispatch player set role event", e);
		}
	}

	void onPlayerJoin(ServerPlayer player) {
		try {
			ServerPlayer newPlayer = addAndSpawnPlayer(player, null, false);
			invoker(GamePlayerEvents.JOIN).onAdd(newPlayer);
		} catch (Exception e) {
			LoveTropics.LOGGER.warn("Failed to dispatch player join event", e);
		}
	}

	ServerPlayer onPlayerLeave(ServerPlayer player, boolean loggingOut) {
		for (PlayerRole role : PlayerRole.ROLES) {
			roles.get(role).remove(player);
		}

		try {
			addedPlayers.remove(player.getUUID());
			invoker(GamePlayerEvents.LEAVE).onRemove(player);
			invoker(GamePlayerEvents.REMOVE).onRemove(player);
		} catch (Exception e) {
			LoveTropics.LOGGER.warn("Failed to dispatch player leave event", e);
		}

		// Don't try to restore the player if they're logging out, as we never save their in-game state anyway
		if (loggingOut) {
			return player;
		}
		return PlayerIsolation.INSTANCE.restore(player);
	}

	public void cancelWithError(Exception exception) {
		LoveTropics.LOGGER.warn("Game canceled due to exception", exception);
		requestStop(GameStopReason.errored(Component.literal("Game stopped due to exception: " + exception)));
	}

	@Override
	public GameResult<Unit> requestStop(GameStopReason reason) {
		if (stopped != null) {
			return GameResult.error(GameTexts.Commands.GAME_ALREADY_STOPPED);
		}

		stopped = reason;

		try {
			invoker(GamePhaseEvents.STOP).stop(reason);

			if (reason.isFinished()) {
				invoker(GamePhaseEvents.FINISH).finish();
			}

			return GameResult.ok();
		} catch (Exception e) {
			return GameResult.fromException("Unknown error while stopping game", e);
		}
	}

	void destroy() {
		if (destroyed) return;
		destroyed = true;

		requestStop(GameStopReason.canceled());

		try {
			for (ServerPlayer player : allPlayers()) {
				addedPlayers.remove(player.getUUID());
				invoker(GamePlayerEvents.REMOVE).onRemove(player);
			}

			invoker(GamePhaseEvents.DESTROY).destroy();
		} catch (Exception e) {
			LoveTropics.LOGGER.warn("Unknown error while stopping game", e);
		} finally {
			map.close(this);
		}
	}

	@Override
	public PlayerSet getPlayersWithRole(PlayerRole role) {
		return roles.get(role);
	}

	@Override
	public MapRegions mapRegions() {
		return map.mapRegions();
	}

	@Override
	public ResourceKey<Level> dimension() {
		return map.dimension();
	}

	@Override
	public ServerLevel level() {
		return server.getLevel(map.dimension());
	}

	@Override
	public GameScheduler scheduler() {
		return scheduler;
	}

	@Override
	public long ticks() {
		return level().getGameTime() - startTime;
	}
}
