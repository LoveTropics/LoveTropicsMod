package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.player.MutablePlayerSet;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.core.game.util.PlayerSnapshot;
import com.lovetropics.minigames.common.core.map.MapRegions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Unit;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.concurrent.CompletableFuture;

public class GamePhase implements IGamePhase {
	final GameInstance game;
	final MinecraftServer server;
	final IGamePhaseDefinition definition;
	final GamePhaseType phaseType;

	final GameMap map;
	final BehaviorMap behaviors;

	final EnumMap<PlayerRole, MutablePlayerSet> roles = new EnumMap<>(PlayerRole.class);

	final GameEventListeners events = new GameEventListeners();

	long startTime;
	GameStopReason stopped;
	boolean destroyed;

	private GamePhase(GameInstance game, IGamePhaseDefinition definition, GamePhaseType phaseType, GameMap map, BehaviorMap behaviors) {
		this.game = game;
		this.server = game.getServer();
		this.definition = definition;
		this.phaseType = phaseType;

		this.map = map;
		this.behaviors = behaviors;

		for (PlayerRole role : PlayerRole.ROLES) {
			MutablePlayerSet rolePlayers = new MutablePlayerSet(server);
			this.roles.put(role, rolePlayers);
		}
	}

	static CompletableFuture<GameResult<GamePhase>> create(GameInstance game, IGamePhaseDefinition definition, GamePhaseType phaseType) {
		MinecraftServer server = game.getServer();

		GameResult<Unit> result = game.lobby.manager.canStartGamePhase(definition);
		if (result.isError()) {
			return CompletableFuture.completedFuture(result.castError());
		}

		CompletableFuture<GameResult<GamePhase>> future = definition.getMap().open(server)
				.thenApplyAsync(r -> r.map(map -> {
					BehaviorMap behaviors = definition.createBehaviors(server);
					return new GamePhase(game, definition, phaseType, map, behaviors);
				}), server);

		return GameResult.handleException("Unknown exception starting game phase", future);
	}

	GameResult<Unit> start() {
		try {
			behaviors.registerTo(this, game.stateMap, events);
		} catch (GameException e) {
			return GameResult.error(e.getTextMessage());
		}

		map.getName().ifPresent(mapName -> getStatistics().global().set(StatisticKey.MAP, mapName));

		startTime = getWorld().getGameTime();

		try {
			invoker(GamePhaseEvents.CREATE).start();

			for (ServerPlayerEntity player : getAllPlayers()) {
				PlayerSnapshot.clearPlayer(player);
				invoker(GamePlayerEvents.ADD).onAdd(player);
				invoker(GamePlayerEvents.SPAWN).onSpawn(player, getRoleFor(player));
			}

			invoker(GamePhaseEvents.START).start();
		} catch (Exception e) {
			return GameResult.fromException("Failed to start game", e);
		}

		return GameResult.ok();
	}

	@Nullable
	GameStopReason tick() {
		try {
			invoker(GamePhaseEvents.TICK).tick();
		} catch (Exception e) {
			cancelWithError(e);
		}
		return stopped;
	}

	@Override
	public IGame getGame() {
		return game;
	}

	@Override
	public GamePhaseType getPhaseType() {
		return phaseType;
	}

	@Override
	public IGamePhaseDefinition getPhaseDefinition() {
		return definition;
	}

	@Override
	public <T> T invoker(GameEventType<T> type) {
		return events.invoker(type);
	}

	@Override
	public boolean setPlayerRole(ServerPlayerEntity player, @Nullable PlayerRole role) {
		PlayerRole lastRole = getRoleFor(player);
		if (role == lastRole) return false;

		if (lastRole != null) {
			roles.get(lastRole).remove(player);
		}
		if (role != null) {
			roles.get(role).add(player);
		}

		onSetPlayerRole(player, role, lastRole);

		return true;
	}

	private void onSetPlayerRole(ServerPlayerEntity player, @Nullable PlayerRole role, @Nullable PlayerRole lastRole) {
		try {
			invoker(GamePlayerEvents.SPAWN).onSpawn(player, role);
			invoker(GamePlayerEvents.SET_ROLE).onSetRole(player, role, lastRole);
		} catch (Exception e) {
			LoveTropics.LOGGER.warn("Failed to dispatch player set role event", e);
		}
	}

	void onPlayerJoin(ServerPlayerEntity player) {
		PlayerSnapshot.clearPlayer(player);

		try {
			invoker(GamePlayerEvents.ADD).onAdd(player);
			invoker(GamePlayerEvents.JOIN).onAdd(player);

			invoker(GamePlayerEvents.SPAWN).onSpawn(player, null);
		} catch (Exception e) {
			LoveTropics.LOGGER.warn("Failed to dispatch player join event", e);
		}
	}

	void onPlayerLeave(ServerPlayerEntity player) {
		for (PlayerRole role : PlayerRole.ROLES) {
			roles.get(role).remove(player);
		}

		try {
			invoker(GamePlayerEvents.LEAVE).onRemove(player);
			invoker(GamePlayerEvents.REMOVE).onRemove(player);
		} catch (Exception e) {
			LoveTropics.LOGGER.warn("Failed to dispatch player leave event", e);
		}
	}

	public void cancelWithError(Exception exception) {
		LoveTropics.LOGGER.warn("Game canceled due to exception", exception);
		this.requestStop(GameStopReason.errored(new StringTextComponent("Game stopped due to exception: " + exception)));
	}

	@Override
	public GameResult<Unit> requestStop(GameStopReason reason) {
		if (stopped != null) {
			return GameResult.error(GameTexts.Commands.gameAlreadyStopped());
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
			for (ServerPlayerEntity player : getAllPlayers()) {
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
	public MapRegions getMapRegions() {
		return map.getRegions();
	}

	@Override
	public RegistryKey<World> getDimension() {
		return map.getDimension();
	}

	@Override
	public ServerWorld getWorld() {
		return server.getLevel(map.getDimension());
	}

	@Override
	public long ticks() {
		return getWorld().getGameTime() - startTime;
	}
}
