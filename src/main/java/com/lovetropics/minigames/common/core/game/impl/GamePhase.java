package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.player.MutablePlayerSet;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.util.Scheduler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Unit;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.concurrent.CompletableFuture;

public class GamePhase implements IGamePhase {
	private final GameInstance game;
	private final MinecraftServer server;
	private final GameMap map;

	private final BehaviorMap behaviors;

	private final EnumMap<PlayerRole, MutablePlayerSet> roles = new EnumMap<>(PlayerRole.class);

	private final GameEventListeners events = new GameEventListeners();

	private long startTime;
	private boolean stopped;

	private GamePhase(GameInstance game, GameMap map, BehaviorMap behaviors) {
		this.game = game;
		this.server = game.getServer();
		this.map = map;

		this.behaviors = behaviors;

		for (PlayerRole role : PlayerRole.ROLES) {
			MutablePlayerSet rolePlayers = new MutablePlayerSet(server);
			this.roles.put(role, rolePlayers);
		}
	}

	static CompletableFuture<GameResult<GamePhase>> start(GameInstance game, IGamePhaseDefinition definition) {
		MinecraftServer server = game.getServer();

		CompletableFuture<GameResult<GamePhase>> future = definition.getMap().open(server)
				.thenComposeAsync(r -> r.thenFlatMap(map -> {
					BehaviorMap behaviors = definition.createBehaviors();
					GamePhase phase = new GamePhase(game, map, behaviors);

					return phase.registerBehaviors()
							.thenFlatMap($ -> phase.start())
							.thenApply(result -> result.map($ -> phase));
				}), server);

		return GameResult.handleException("Unknown exception starting game phase", future);
	}

	private GameResult<Unit> registerBehaviors() {
		try {
			for (IGameBehavior behavior : behaviors) {
				behavior.registerState(this, game.stateMap);
			}

			for (IGameBehavior behavior : behaviors) {
				behavior.register(this, events);
			}

			return GameResult.ok();
		} catch (GameException e) {
			return GameResult.error(e.getTextMessage());
		}
	}

	private CompletableFuture<GameResult<Unit>> start() {
		this.map.getName().ifPresent(mapName -> this.getStatistics().global().set(StatisticKey.MAP, mapName));

		try {
			invoker(GamePhaseEvents.INITIALIZE).start();

			for (ServerPlayerEntity player : getAllPlayers()) {
				invoker(GamePlayerEvents.ADD).onAdd(player);
			}
		} catch (Exception e) {
			return CompletableFuture.completedFuture(GameResult.fromException("Failed to dispatch game pre-start event", e));
		}

		return Scheduler.nextTick().supply(s -> {
			this.startTime = getWorld().getGameTime();
			try {
				invoker(GamePhaseEvents.START).start();
				return GameResult.ok();
			} catch (Exception e) {
				return GameResult.fromException("Failed to dispatch game start event", e);
			}
		});
	}

	boolean tick() {
		try {
			invoker(GamePhaseEvents.TICK).tick();
		} catch (Exception e) {
			stopWithError(e);
		}

		return !stopped;
	}

	@Override
	public IGameInstance getGame() {
		return game;
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
			invoker(GamePlayerEvents.SET_ROLE).onSetRole(player, role, lastRole);
		} catch (Exception e) {
			LoveTropics.LOGGER.warn("Failed to dispatch player set role event", e);
		}

		// TODO: Implement
		/*LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(lobby.getId().getNetworkId(), role));
		sendPlayerCountUpdate(role);
		sendPlayerCountUpdate(lastRole);*/
	}

	void onPlayerJoin(ServerPlayerEntity player) {
		try {
			invoker(GamePlayerEvents.JOIN).onAdd(player);
			invoker(GamePlayerEvents.ADD).onAdd(player);
		} catch (Exception e) {
			LoveTropics.LOGGER.warn("Failed to dispatch player join event", e);
		}

		// TODO: implement
		/*LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(getLobbyId().networkId, role));
		sendPlayerCountUpdate(role);*/
	}

	void onPlayerLeave(ServerPlayerEntity player) {
		try {
			invoker(GamePlayerEvents.LEAVE).onRemove(player);
			invoker(GamePlayerEvents.REMOVE).onRemove(player);
		} catch (Exception e) {
			LoveTropics.LOGGER.warn("Failed to dispatch player leave event", e);
		}
	}

	public void stopWithError(Exception exception) {
		// TODO: pass up errors?
		LoveTropics.LOGGER.error("Game stopping due to exception", exception);
		this.stop(GameStopReason.ERRORED);
	}

	@Override
	public GameResult<Unit> stop(GameStopReason reason) {
		if (stopped) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME));
		}

		stopped = true;

		try {
			invoker(GamePhaseEvents.STOP).stop(reason);

			for (ServerPlayerEntity player : getAllPlayers()) {
				invoker(GamePlayerEvents.REMOVE).onRemove(player);
			}

			return GameResult.ok();
		} catch (Exception e) {
			return GameResult.fromException("Unknown error stopping game", e);
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
		return server.getWorld(map.getDimension());
	}

	@Override
	public long ticks() {
		return getWorld().getGameTime() - startTime;
	}
}
