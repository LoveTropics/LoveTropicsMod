package com.lovetropics.minigames.common.core.game.polling;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.client.minigame.ClientRoleMessage;
import com.lovetropics.minigames.client.minigame.PlayerCountsMessage;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePollingEvents;
import com.lovetropics.minigames.common.core.game.control.GameControlCommands;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public final class PollingGameInstance implements ProtoGame {
	private final MinecraftServer server;
	private final IGameDefinition definition;
	private final PlayerKey initiator;

	private final BehaviorMap behaviors;
	private final GameEventListeners events = new GameEventListeners();
	private final GameControlCommands controlCommands;

	private final GameRegistrations registrations = new GameRegistrations();

	private PollingGameInstance(MinecraftServer server, IGameDefinition definition, PlayerKey initiator) {
		this.server = server;
		this.definition = definition;
		this.behaviors = definition.createBehaviors();
		this.initiator = initiator;

		this.controlCommands = new GameControlCommands(initiator);
	}

	public static GameResult<PollingGameInstance> create(MinecraftServer server, IGameDefinition definition, PlayerKey initiator) {
		PollingGameInstance instance = new PollingGameInstance(server, definition, initiator);

		for (IGameBehavior behavior : instance.behaviors) {
			try {
				behavior.registerPolling(instance, instance.events);
			} catch (GameException e) {
				return GameResult.error(e.getTextMessage());
			}
		}

		return GameResult.ok(instance);
	}

	@Override
	public GameStatus getStatus() {
		return GameStatus.POLLING;
	}

	public GameResult<ITextComponent> joinPlayerAs(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		if (registrations.contains(player.getUniqueID())) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_MINIGAME_ALREADY_REGISTERED));
		}

		try {
			invoker(GamePollingEvents.PLAYER_REGISTER).onPlayerRegister(this, player, requestedRole);
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch player register event", e);
		}

		registrations.add(player.getUniqueID(), requestedRole);

		PlayerSet serverPlayers = PlayerSet.ofServer(server);
		GameMessages gameMessages = GameMessages.forGame(definition);

		if (registrations.participantCount() == definition.getMinimumParticipantCount()) {
			serverPlayers.sendMessage(gameMessages.enoughPlayers());
		}

		serverPlayers.sendMessage(gameMessages.playerJoined(player, requestedRole));

		PlayerRole trueRole = requestedRole == null ? PlayerRole.PARTICIPANT : requestedRole;
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(trueRole));
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerCountsMessage(trueRole, getMemberCount(trueRole)));

		return GameResult.ok(gameMessages.registerSuccess());
	}

	public GameResult<ITextComponent> removePlayer(ServerPlayerEntity player) {
		if (!registrations.remove(player.getUniqueID())) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NOT_REGISTERED_FOR_MINIGAME));
		}

		GameMessages gameMessages = GameMessages.forGame(definition);
		if (registrations.participantCount() == definition.getMinimumParticipantCount() - 1) {
			PlayerSet.ofServer(server).sendMessage(gameMessages.noLongerEnoughPlayers());
		}

		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(null));
		for (PlayerRole role : PlayerRole.ROLES) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerCountsMessage(role, getMemberCount(role)));
		}

		return GameResult.ok(gameMessages.unregisterSuccess());
	}

	public CompletableFuture<GameResult<GameInstance>> start() {
		return definition.getMapProvider().open(server)
				.thenComposeAsync(result -> {
					if (result.isOk()) {
						GameMap map = result.getOk();
						return GameInstance.start(server, definition, map, behaviors, initiator, registrations);
					} else {
						return CompletableFuture.completedFuture(result.castError());
					}
				}, server)
				.handle((result, throwable) -> {
					if (throwable instanceof Exception) {
						return GameResult.fromException("Unknown error starting minigame", (Exception) throwable);
					}
					return result;
				});
	}

	@Override
	public MinecraftServer getServer() {
		return server;
	}

	@Override
	public IGameDefinition getDefinition() {
		return definition;
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
	public int getMemberCount(PlayerRole role) {
		// TODO extensible
		return role == PlayerRole.PARTICIPANT ? registrations.participantCount() : registrations.spectatorCount();
	}

	public boolean isPlayerRegistered(ServerPlayerEntity player) {
		return registrations.contains(player.getUniqueID());
	}
}
