package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.client.lobby.state.message.JoinedLobbyMessage;
import com.lovetropics.minigames.client.lobby.state.message.LeftLobbyMessage;
import com.lovetropics.minigames.client.lobby.state.message.LobbyPlayersMessage;
import com.lovetropics.minigames.client.lobby.state.message.LobbyUpdateMessage;
import com.lovetropics.minigames.common.core.game.GameRegistrations;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.GameLobbyMetadata;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.LobbyGameQueue;
import com.lovetropics.minigames.common.core.game.lobby.LobbyVisibility;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.instances.control.ControlCommandInvoker;
import com.lovetropics.minigames.common.core.game.util.GameMessages;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// TODO: do we want a different game lobby implementation for something like carnival games?
public final class GameLobby implements IGameLobby {
	private final MultiGameManager manager;
	private final MinecraftServer server;
	private final GameLobbyMetadata metadata;

	private final GameRegistrations registrations;

	private final LobbyVisibility visibility = LobbyVisibility.PRIVATE;

	final LobbyGameQueue gameQueue = new LobbyGameQueue();

	final LobbyState.Factory states = new LobbyState.Factory(this);
	LobbyState state = states.paused();

	GameLobby(MultiGameManager manager, MinecraftServer server, GameLobbyMetadata metadata) {
		this.manager = manager;
		this.server = server;
		this.metadata = metadata;

		this.registrations = new GameRegistrations(server);
	}

	@Override
	public MinecraftServer getServer() {
		return server;
	}

	@Override
	public GameLobbyMetadata getMetadata() {
		return this.metadata;
	}

	@Override
	public PlayerSet getAllPlayers() {
		return registrations;
	}

	@Nullable
	@Override
	public PlayerRole getRegisteredRoleFor(ServerPlayerEntity player) {
		return registrations.getRoleFor(player.getUniqueID());
	}

	@Override
	public LobbyGameQueue getGameQueue() {
		return gameQueue;
	}

	@Nullable
	@Override
	public IActiveGame getActiveGame() {
		return state.getActiveGame();
	}

	@Override
	public ControlCommandInvoker getControlCommands() {
		return state.getControlCommands();
	}

	// TODO: publish state to all tracking players when visibility changes
	@Override
	public boolean isVisibleTo(CommandSource source) {
		if (source.hasPermissionLevel(2) || metadata.initiator().matches(source.getEntity())) {
			return true;
		}

		return state.isAccessible() && visibility == LobbyVisibility.PUBLIC;
	}

	boolean tick() {
		LobbyState nextState = state.tick();
		if (nextState != state) {
			state = nextState;
			// TODO: check where we send this
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), LobbyUpdateMessage.update(this));
		}

		return nextState != null;
	}

	void stop() {
		PlayerSet.ofServer(server).sendMessage(GameMessages.forLobby(this).stopPolling()); // TODO: polling message?

		state = states.stopped();
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), LobbyUpdateMessage.remove(this));

		manager.removeLobby(this);
	}

	void collectRegistrations(Collection<ServerPlayerEntity> participants, Collection<ServerPlayerEntity> spectators, IGameDefinition game) {
		registrations.collectInto(participants, spectators, game.getMaximumParticipantCount());
	}

	@Override
	public boolean registerPlayer(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		if (!registrations.add(player.getUniqueID(), requestedRole)) {
			return false;
		}

		state.onPlayerRegister(player, requestedRole);

		// TODO: extract out all notifications / packet logic?
		PlayerSet serverPlayers = PlayerSet.ofServer(server);
		GameMessages gameMessages = GameMessages.forLobby(this);

		// TODO: how do we want to manage these?
		/*if (registrations.participantCount() == definition.getMinimumParticipantCount()) {
			serverPlayers.sendMessage(gameMessages.enoughPlayers());
		}*/

		serverPlayers.sendMessage(gameMessages.playerJoined(player, requestedRole));

		// TODO: setting roles within the active game must also send update packets, but we don't want to duplicate
		PlayerRole trueRole = requestedRole == null ? PlayerRole.PARTICIPANT : requestedRole;
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), JoinedLobbyMessage.create(this, trueRole));
		LoveTropicsNetwork.CHANNEL.send(this.trackingPlayers(), LobbyPlayersMessage.add(this, Collections.singleton(player)));

		return true;
	}

	@Override
	public boolean removePlayer(ServerPlayerEntity player) {
		if (!registrations.remove(player.getUniqueID())) {
			return false;
		}

		/*GameMessages gameMessages = GameMessages.forLobby(this);
		if (registrations.participantCount() == definition.getMinimumParticipantCount() - 1) {
			PlayerSet.ofServer(server).sendMessage(gameMessages.noLongerEnoughPlayers());
		}*/

		//TODO
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new LeftLobbyMessage());
		LoveTropicsNetwork.CHANNEL.send(this.trackingPlayers(), LobbyPlayersMessage.remove(this, Collections.singleton(player)));

		return true;
	}

	private PacketDistributor.PacketTarget trackingPlayers() {
		return PacketDistributor.NMLIST.with(() -> {
			List<NetworkManager> tracking = new ArrayList<>();
			for (ServerPlayerEntity player : server.getPlayerList().getPlayers()) {
				if (isVisibleTo(player.getCommandSource())) {
					tracking.add(player.connection.netManager);
				}
			}
			return tracking;
		});
	}

	@Override
	public GameResult<Unit> requestStart() {
		return state.requestStart();
	}
}
