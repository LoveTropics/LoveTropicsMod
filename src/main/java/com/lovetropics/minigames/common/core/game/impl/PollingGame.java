package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.minigame.ClientRoleMessage;
import com.lovetropics.minigames.client.minigame.PlayerCountsMessage;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePollingEvents;
import com.lovetropics.minigames.common.core.game.control.GameControlCommands;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class PollingGame implements IPollingGame {
	private final GameInstance instance;
	private final MinecraftServer server;

	private final BehaviorMap behaviors;
	private final GameEventListeners events = new GameEventListeners();
	private final GameControlCommands controlCommands;

	private final GameRegistrations registrations;

	private PollingGame(GameInstance instance) {
		this.instance = instance;
		this.server = instance.getServer();
		this.behaviors = instance.getDefinition().createBehaviors();

		this.controlCommands = new GameControlCommands(instance.getInitiator());
		this.registrations = new GameRegistrations(server);
	}

	static GameResult<PollingGame> create(GameInstance instance) {
		PollingGame polling = new PollingGame(instance);

		for (IGameBehavior behavior : polling.behaviors) {
			try {
				behavior.registerPolling(polling, polling.events);
			} catch (GameException e) {
				return GameResult.error(e.getTextMessage());
			}
		}

		return GameResult.ok(polling);
	}

	@Override
	public GameInstance getInstance() {
		return instance;
	}

	@Override
	public boolean requestPlayerJoin(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		if (registrations.contains(player.getUniqueID())) {
			return false;
		}

		try {
			invoker(GamePollingEvents.PLAYER_REGISTER).onPlayerRegister(this, player, requestedRole);
		} catch (Exception e) {
			LoveTropics.LOGGER.warn("Failed to dispatch player register event", e);
		}

		registrations.add(player.getUniqueID(), requestedRole);

		PlayerSet serverPlayers = PlayerSet.ofServer(server);
		GameMessages gameMessages = GameMessages.forGame(instance);

		if (registrations.participantCount() == instance.getDefinition().getMinimumParticipantCount()) {
			serverPlayers.sendMessage(gameMessages.enoughPlayers());
		}

		serverPlayers.sendMessage(gameMessages.playerJoined(player, requestedRole));

		PlayerRole trueRole = requestedRole == null ? PlayerRole.PARTICIPANT : requestedRole;
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(trueRole));
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerCountsMessage(trueRole, getMemberCount(trueRole)));

		return true;
	}

	@Override
	public boolean removePlayer(ServerPlayerEntity player) {
		if (!registrations.remove(player.getUniqueID())) {
			return false;
		}

		GameMessages gameMessages = GameMessages.forGame(instance);
		if (registrations.participantCount() == instance.getDefinition().getMinimumParticipantCount() - 1) {
			PlayerSet.ofServer(server).sendMessage(gameMessages.noLongerEnoughPlayers());
		}

		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(null));
		for (PlayerRole role : PlayerRole.ROLES) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerCountsMessage(role, getMemberCount(role)));
		}

		return true;
	}

	@Override
	public CompletableFuture<GameResult<IActiveGame>> start() {
		return instance.getDefinition().getMapProvider().open(server)
				.thenComposeAsync(result -> {
					if (result.isOk()) {
						GameMap map = result.getOk();

						List<ServerPlayerEntity> participants = new ArrayList<>();
						List<ServerPlayerEntity> spectators = new ArrayList<>();

						registrations.collectInto(instance.getServer(), participants, spectators, instance.getDefinition().getMaximumParticipantCount());

						return ActiveGame.start(instance, map, behaviors, participants, spectators);
					} else {
						return CompletableFuture.completedFuture(result.castError());
					}
				}, server)
				.handle((result, throwable) -> {
					if (throwable instanceof Exception) {
						return GameResult.<ActiveGame>fromException("Unknown error starting game", (Exception) throwable);
					}
					return result;
				})
				.thenApply(result -> {
					if (result.isOk()) {
						ActiveGame resultGame = result.getOk();
						instance.setPhase(resultGame);
						return GameResult.ok(resultGame);
					} else {
						return result.castError();
					}
				});
	}

	@Override
	public GameResult<Unit> cancel() {
		PlayerSet.ofServer(server).sendMessage(GameMessages.forGame(this).stopPolling());
		instance.stop();

		return GameResult.ok();
	}

	@Override
	public BehaviorMap getBehaviors() {
		return behaviors;
	}

	@Override
	public GameControlCommands getControlCommands() {
		return controlCommands;
	}

	@Override
	public GameEventListeners getEvents() {
		return this.events;
	}

	@Override
	public PlayerSet getAllPlayers() {
		return registrations;
	}

	@Override
	public int getMemberCount(PlayerRole role) {
		// TODO extensible
		return role == PlayerRole.PARTICIPANT ? registrations.participantCount() : registrations.spectatorCount();
	}
}
