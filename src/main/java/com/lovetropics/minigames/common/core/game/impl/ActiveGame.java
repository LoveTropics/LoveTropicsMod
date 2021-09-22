package com.lovetropics.minigames.common.core.game.impl;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.player.MutablePlayerSet;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.instances.control.ControlCommandInvoker;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.util.GameMessages;
import com.lovetropics.minigames.common.core.integration.GameInstanceTelemetry;
import com.lovetropics.minigames.common.core.integration.Telemetry;
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
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Default implementation of a minigame instance. Simple and naive
 * solution to holding state of players that are currently part
 * of the minigame instance, as well as the definition that is being
 * used to specify the rulesets for the minigame.
 */
public class ActiveGame implements IActiveGame {
	private final GameLobby lobby;
	private final MinecraftServer server;
	private final GameMap map;
	private final BehaviorMap behaviors;

	private final MutablePlayerSet allPlayers;
	private final EnumMap<PlayerRole, MutablePlayerSet> roles = new EnumMap<>(PlayerRole.class);

	private final GameInstanceTelemetry telemetry;

	private final GameEventListeners events = new GameEventListeners();
	private final GameStateMap state = new GameStateMap();
	private final GameStatistics statistics = new GameStatistics();

	private final ControlCommandInvoker controlCommands = ControlCommandInvoker.forGame(this);

	private long startTime;

	private boolean stopped;

	private ActiveGame(GameLobby lobby, GameMap map, BehaviorMap behaviors) {
		this.lobby = lobby;
		this.server = lobby.getServer();
		this.map = map;
		this.behaviors = behaviors;

		this.allPlayers = new MutablePlayerSet(server);

		for (PlayerRole role : PlayerRole.ROLES) {
			MutablePlayerSet rolePlayers = new MutablePlayerSet(server);
			this.roles.put(role, rolePlayers);
		}

		this.telemetry = Telemetry.INSTANCE.openGame(this);
	}

	static CompletableFuture<GameResult<ActiveGame>> start(
			GameLobby lobby, GameMap map, BehaviorMap behaviors,
			List<ServerPlayerEntity> participants, List<ServerPlayerEntity> spectators
	) {
		ActiveGame game = new ActiveGame(lobby, map, behaviors);

		GameResult<Unit> result = game.registerBehaviors();
		if (result.isError()) {
			return CompletableFuture.completedFuture(result.castError());
		}

		result = game.addPlayers(participants, spectators);
		if (result.isError()) {
			return CompletableFuture.completedFuture(result.castError());
		}

		map.getName().ifPresent(mapName -> game.getStatistics().getGlobal().set(StatisticKey.MAP, mapName));
		game.telemetry.start(game.events);

		return Scheduler.INSTANCE.submit(s -> {
			return game.start().map(u -> game);
		}, 1);
	}

	private GameResult<Unit> registerBehaviors() {
		try {
			for (IGameBehavior behavior : behaviors) {
				behavior.registerState(state);
			}

			for (IGameBehavior behavior : behaviors) {
				behavior.register(this, events);
			}

			return GameResult.ok();
		} catch (GameException e) {
			return GameResult.error(e);
		}
	}

	private GameResult<Unit> addPlayers(List<ServerPlayerEntity> participants, List<ServerPlayerEntity> spectators) {
		try {
			invoker(GameLifecycleEvents.ASSIGN_ROLES).assignRoles(this, participants, spectators);
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch assign roles event", e);
		}

		for (ServerPlayerEntity player : participants) {
			addPlayer(player, PlayerRole.PARTICIPANT);
		}

		for (ServerPlayerEntity player : spectators) {
			addPlayer(player, PlayerRole.SPECTATOR);
		}

		return GameResult.ok();
	}

	private GameResult<Unit> start() {
		startTime = getWorld().getGameTime();
		try {
			invoker(GameLifecycleEvents.START).start(this);
			return GameResult.ok();
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch game start event", e);
		}
	}

	boolean tick() {
		try {
			invoker(GameLifecycleEvents.TICK).tick(this);
		} catch (Exception e) {
			LoveTropics.LOGGER.warn("Failed to dispatch game tick event", e);
			stop(GameStopReason.ERRORED);
		}

		return !stopped;
	}

	@Override
	public IGameLobby getLobby() {
		return lobby;
	}

	@Override
	public boolean requestPlayerJoin(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		return addPlayer(player, PlayerRole.SPECTATOR);
	}

	public boolean addPlayer(ServerPlayerEntity player, PlayerRole role) {
		if (allPlayers.add(player)) {
			roles.get(role).add(player);

			try {
				invoker(GamePlayerEvents.JOIN).onJoin(this, player, role);
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player join event", e);
			}

			// TODO: implement
			/*LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(getLobbyId().networkId, role));
			sendPlayerCountUpdate(role);*/

			return true;
		} else {
			return setPlayerRole(player, role);
		}
	}

	@Override
	public boolean setPlayerRole(ServerPlayerEntity player, PlayerRole role) {
		PlayerRole lastRole = getRoleFor(player);
		if (lastRole == null) {
			return false;
		}

		if (lastRole != role) {
			roles.get(role).add(player);
			roles.get(lastRole).remove(player);

			try {
				invoker(GamePlayerEvents.CHANGE_ROLE).onChangeRole(this, player, role, lastRole);
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player change role event", e);
			}

			// TODO: Implement
			/*LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(lobby.getId().getNetworkId(), role));
			sendPlayerCountUpdate(role);
			sendPlayerCountUpdate(lastRole);*/

			return true;
		} else {
			return false;
		}
	}

	// TODO: implement
	/*private void sendPlayerCountUpdate(PlayerRole role) {
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerCountsMessage(lobby.getId().getNetworkId(), role, getMemberCount(role)));
	}*/

	@Nullable
	private PlayerRole getRoleFor(ServerPlayerEntity player) {
		for (PlayerRole role : PlayerRole.ROLES) {
			if (roles.get(role).contains(player)) {
				return role;
			}
		}
		return null;
	}

	@Override
	public boolean removePlayer(ServerPlayerEntity player) {
		if (allPlayers.remove(player)) {
			for (MutablePlayerSet rolePlayers : roles.values()) {
				rolePlayers.remove(player);
			}

			try {
				invoker(GamePlayerEvents.LEAVE).onLeave(this, player);
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player leave event", e);
			}

			return true;
		}

		return false;
	}

	@Override
	public PlayerSet getAllPlayers() {
		return allPlayers;
	}

	@Override
	public GameResult<Unit> stop(GameStopReason reason) {
		if (stopped) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME));
		}

		stopped = true;

		try {
			invoker(GameLifecycleEvents.STOP).stop(this, reason);

			List<ServerPlayerEntity> players = Lists.newArrayList(getAllPlayers());
			for (ServerPlayerEntity player : players) {
				removePlayer(player);
			}

			// TODO: update telemetry usage
			if (reason.isFinished()) {
				telemetry.finish(statistics);
			} else {
				telemetry.cancel();
			}

			PlayerSet.ofServer(server).sendMessage(GameMessages.forLobby(lobby).finished());

			invoker(GameLifecycleEvents.POST_STOP).stop(this, reason);

			return GameResult.ok();
		} catch (Exception e) {
			telemetry.cancel();
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

	@Override
	public <T> T invoker(GameEventType<T> type) {
		return events.invoker(type);
	}

	@Override
	public GameStateMap getState() {
		return state;
	}

	@Override
	public GameStatistics getStatistics() {
		return statistics;
	}

	@Override
	public GameInstanceTelemetry getTelemetry() {
		return telemetry;
	}

	@Override
	public ControlCommandInvoker getControlCommands() {
		return controlCommands;
	}
}
