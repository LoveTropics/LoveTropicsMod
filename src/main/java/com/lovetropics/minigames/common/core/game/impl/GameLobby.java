package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.client.minigame.ClientGameLobbyMessage;
import com.lovetropics.minigames.client.minigame.ClientRoleMessage;
import com.lovetropics.minigames.client.minigame.PlayerCountsMessage;
import com.lovetropics.minigames.common.core.game.GameRegistrations;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IActiveGame;
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
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

// TODO: do we want a different game lobby implementation for something like carnival games?
public final class GameLobby implements IGameLobby {
	private final MultiGameManager manager;
	private final MinecraftServer server;
	private final GameLobbyMetadata metadata;

	final GameRegistrations registrations;

	// TODO: private by default
	private final LobbyVisibility visibility = LobbyVisibility.PUBLIC;

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
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), ClientGameLobbyMessage.update(this));
		}

		return nextState != null;
	}

	void stop() {
		PlayerSet.ofServer(server).sendMessage(GameMessages.forLobby(this).stopPolling()); // TODO: polling message?

		state = states.stopped();
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), ClientGameLobbyMessage.stop(metadata.id()));

		manager.removeLobby(this);
	}

	public int getMemberCount(PlayerRole role) {
		// TODO extensible
		return role == PlayerRole.PARTICIPANT ? registrations.participantCount() : registrations.spectatorCount();
	}

	@Override
	public boolean registerPlayer(ServerPlayerEntity player, @Nullable PlayerRole requestedRole) {
		if (!registrations.add(player.getUniqueID(), requestedRole)) {
			return false;
		}

		// TODO: all states need to handle player registration really
		state.registerPlayer(player, requestedRole);

		// TODO: extract out all notifications / packet logic?
		PlayerSet serverPlayers = PlayerSet.ofServer(server);
		GameMessages gameMessages = GameMessages.forLobby(this);

		// TODO: how do we want to manage these?
		/*if (registrations.participantCount() == definition.getMinimumParticipantCount()) {
			serverPlayers.sendMessage(gameMessages.enoughPlayers());
		}*/

		serverPlayers.sendMessage(gameMessages.playerJoined(player, requestedRole));

		int networkId = metadata.id().networkId();

		PlayerRole trueRole = requestedRole == null ? PlayerRole.PARTICIPANT : requestedRole;
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(networkId, trueRole));
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerCountsMessage(networkId, trueRole, getMemberCount(trueRole)));

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

		int networkId = metadata.id().networkId();

		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ClientRoleMessage(networkId, null));
		for (PlayerRole role : PlayerRole.ROLES) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), new PlayerCountsMessage(networkId, role, getMemberCount(role)));
		}

		return true;
	}

	@Override
	public GameResult<Unit> requestStart() {
		return state.requestStart();
	}
}
