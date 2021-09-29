package com.lovetropics.minigames.common.core.game.impl;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.client.lobby.state.message.JoinedLobbyMessage;
import com.lovetropics.minigames.client.lobby.state.message.LeftLobbyMessage;
import com.lovetropics.minigames.client.lobby.state.message.LobbyPlayersMessage;
import com.lovetropics.minigames.client.lobby.state.message.LobbyUpdateMessage;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IGame;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.PlayerIsolation;
import com.lovetropics.minigames.common.core.game.lobby.*;
import com.lovetropics.minigames.common.core.game.player.PlayerIterable;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

// TODO: do we want a different game lobby implementation for something like carnival games?
final class GameLobby implements IGameLobby {
	final MultiGameManager manager;
	final MinecraftServer server;
	GameLobbyMetadata metadata;

	final LobbyGameQueue gameQueue;
	final LobbyPlayerManager players;
	final LobbyManagement management;
	final LobbyTrackingPlayers trackingPlayers;

	LobbyVisibility visibility = LobbyVisibility.PRIVATE;

	final LobbyStateListener stateListener = LobbyStateListener.compose(
			new NetworkUpdateListener(),
			new ChatNotifyListener()
	);

	final PlayerIsolation playerIsolation = new PlayerIsolation();

	LobbyState state = pausedState();
	private boolean closed;

	GameLobby(MultiGameManager manager, MinecraftServer server, GameLobbyMetadata metadata) {
		this.manager = manager;
		this.server = server;
		this.metadata = metadata;

		this.gameQueue = new LobbyGameQueue(this);
		this.players = new LobbyPlayerManager(this);
		this.management = new LobbyManagement(this);
		this.trackingPlayers = new LobbyTrackingPlayers(this);
	}

	LobbyState pausedState() {
		LobbyControls controls = new LobbyControls()
				.add(LobbyControls.Type.PLAY, () -> {
					LobbyState newState = this.gameQueue.tryResume();
					if (newState != null) {
						this.setState(newState);
					}
					return GameResult.ok();
				});

		return new LobbyState(null, controls);
	}

	LobbyState closedState() {
		return new LobbyState(null, LobbyControls.empty());
	}

	@Override
	public MinecraftServer getServer() {
		return server;
	}

	@Override
	public GameLobbyMetadata getMetadata() {
		return metadata;
	}

	@Override
	public LobbyPlayerManager getPlayers() {
		return players;
	}

	@Override
	public LobbyGameQueue getGameQueue() {
		return gameQueue;
	}

	@Nullable
	@Override
	public IGamePhase getCurrentPhase() {
		return state.phase();
	}

	@Override
	public LobbyControls getControls() {
		return state.controls();
	}

	@Override
	public ILobbyManagement getManagement() {
		return management;
	}

	@Override
	public PlayerIterable getTrackingPlayers() {
		return trackingPlayers;
	}

	@Override
	public boolean isVisibleTo(CommandSource source) {
		if (management.canManage(source)) {
			return true;
		}

		return state.game() != null && visibility.isPublic();
	}

	@Override
	public LobbyVisibility getVisibility() {
		return visibility;
	}

	void setName(String name) {
		metadata = manager.renameLobby(metadata, name);
	}

	void setVisibility(LobbyVisibility visibility) {
		this.visibility = visibility;
		this.trackingPlayers.rebuildTracking();
	}

	void tick() {
		GameResult<Unit> result = tickState();
		if (result.isError()) {
			// TODO: handle error
			this.setState(pausedState());
		}
	}

	private GameResult<Unit> tickState() {
		GameResult<LobbyState> tickResult = gameQueue.tick(this::pausedState);
		if (tickResult != null) {
			return tickResult.andThen(this::setState);
		} else {
			return GameResult.ok();
		}
	}

	private GameResult<Unit> setState(LobbyState newState) {
		LobbyState oldState = state;
		state = newState;
		return onGameStateChange(oldState, newState);
	}

	GameResult<Unit> onGameStateChange(LobbyState oldState, LobbyState newState) {
		GameResult<Unit> result = GameResult.ok();

		GamePhase oldPhase = oldState.phase();
		GamePhase newPhase = newState.phase();
		if (oldPhase != newPhase) {
			result = onGamePhaseChanged(oldPhase, newPhase);
		}

		management.onGameStateChange();

		return result;
	}

	private GameResult<Unit> onGamePhaseChanged(GamePhase oldPhase, GamePhase newPhase) {
		GameResult<Unit> result = GameResult.ok();

		if (newPhase != null && oldPhase == null) {
			onQueueResume();
		} else if (newPhase == null && oldPhase != null) {
			onQueuePaused();
		}

		if (newPhase != null) {
			manager.addGamePhaseToDimension(newPhase.getDimension(), newPhase);
			result = newPhase.start();
		}

		if (oldPhase != null) {
			oldPhase.destroy();
			manager.removeGamePhaseFromDimension(oldPhase.getDimension(), oldPhase);
		}

		stateListener.onGamePhaseChange(this);

		return result;
	}

	void onQueueResume() {
		for (ServerPlayerEntity player : getPlayers()) {
			onPlayerEnterGame(player);
		}
	}

	void onQueuePaused() {
		for (ServerPlayerEntity player : getPlayers()) {
			onPlayerExitGame(player);
		}

		stateListener.onLobbyPaused(this);
	}

	void onPlayerLoggedIn(ServerPlayerEntity player) {
		trackingPlayers.onPlayerLoggedIn(player);
	}

	void onPlayerLoggedOut(ServerPlayerEntity player) {
		trackingPlayers.onPlayerLoggedOut(player);

		players.remove(player);
	}

	void onPlayerRegister(ServerPlayerEntity player) {
		manager.addPlayerToLobby(player, this);

		GamePhase phase = state.phase();
		if (phase != null) {
			onPlayerEnterGame(player);
			phase.onPlayerJoin(player);
		}

		PlayerRole role = players.getRegisteredRoleFor(player);
		stateListener.onPlayerJoin(this, player, role);
	}

	void onPlayerLeave(ServerPlayerEntity player) {
		GamePhase phase = state.phase();
		if (phase != null) {
			phase.onPlayerLeave(player);
			onPlayerExitGame(player);
		}

		stateListener.onPlayerLeave(this, player);
		management.stopManaging(player);

		manager.removePlayerFromLobby(player, this);
	}

	// TODO: better abstract this logic?
	void onPlayerEnterGame(ServerPlayerEntity player) {
		playerIsolation.accept(player);
	}

	void onPlayerExitGame(ServerPlayerEntity player) {
		playerIsolation.restore(player);
	}

	void onPlayerStartTracking(ServerPlayerEntity player) {
		stateListener.onPlayerStartTracking(this, player);
	}

	void onPlayerStopTracking(ServerPlayerEntity player) {
		stateListener.onPlayerStopTracking(this, player);
	}

	void close() {
		if (closed) return;
		closed = true;

		management.disable();

		try {
			setState(closedState());

			LobbyPlayerManager players = getPlayers();
			for (ServerPlayerEntity player : Lists.newArrayList(players)) {
				players.remove(player);
			}

			stateListener.onLobbyStop(this);
			gameQueue.clear();
		} finally {
			manager.removeLobby(this);
		}
	}

	// TODO: reduce message clutter and use toasts where possible
	static final class ChatNotifyListener implements LobbyStateListener {
		@Override
		public void onPlayerJoin(IGameLobby lobby, ServerPlayerEntity player, PlayerRole registeredRole) {
			IGame currentGame = lobby.getCurrentGame();
			if (currentGame != null) {
				onPlayerJoinGame(lobby, currentGame);
			}

			ITextComponent message = GameTexts.Status.playerJoined(lobby, player, registeredRole);
			lobby.getTrackingPlayers().sendMessage(message);
		}

		@Override
		public void onPlayerLeave(IGameLobby lobby, ServerPlayerEntity player) {
			IGame currentGame = lobby.getCurrentGame();
			if (currentGame != null) {
				onPlayerLeaveGame(lobby, currentGame);
			}
		}

		@Override
		public void onPlayerStartTracking(IGameLobby lobby, ServerPlayerEntity player) {
			player.sendStatusMessage(GameTexts.Status.lobbyOpened(lobby), false);
		}

		private void onPlayerJoinGame(IGameLobby lobby, IGame currentGame) {
			int minimumParticipants = currentGame.getDefinition().getMinimumParticipantCount();
			if (lobby.getPlayers().getParticipantCount() == minimumParticipants) {
				ITextComponent enoughPlayers = GameTexts.Status.enoughPlayers();
				lobby.getTrackingPlayers().sendMessage(enoughPlayers);
			}
		}

		private void onPlayerLeaveGame(IGameLobby lobby, IGame currentGame) {
			int minimumParticipants = currentGame.getDefinition().getMinimumParticipantCount();
			if (lobby.getPlayers().getParticipantCount() == minimumParticipants - 1) {
				ITextComponent noLongerEnoughPlayers = GameTexts.Status.noLongerEnoughPlayers();
				lobby.getTrackingPlayers().sendMessage(noLongerEnoughPlayers);
			}
		}

		@Override
		public void onLobbyPaused(IGameLobby lobby) {
			lobby.getPlayers().sendMessage(GameTexts.Status.lobbyPaused());
		}

		@Override
		public void onLobbyStop(IGameLobby lobby) {
			lobby.getPlayers().sendMessage(GameTexts.Status.lobbyStopped());
		}
	}

	static final class NetworkUpdateListener implements LobbyStateListener {
		@Override
		public void onPlayerJoin(IGameLobby lobby, ServerPlayerEntity player, PlayerRole registeredRole) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), JoinedLobbyMessage.create(lobby, registeredRole));
			lobby.getTrackingPlayers().sendPacket(LoveTropicsNetwork.CHANNEL, LobbyPlayersMessage.update(lobby));
		}

		@Override
		public void onPlayerLeave(IGameLobby lobby, ServerPlayerEntity player) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new LeftLobbyMessage());
			lobby.getTrackingPlayers().sendPacket(LoveTropicsNetwork.CHANNEL, LobbyPlayersMessage.update(lobby));
		}

		@Override
		public void onPlayerStartTracking(IGameLobby lobby, ServerPlayerEntity player) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), LobbyUpdateMessage.update(lobby));
		}

		@Override
		public void onPlayerStopTracking(IGameLobby lobby, ServerPlayerEntity player) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), LobbyUpdateMessage.remove(lobby));
		}

		@Override
		public void onLobbyStop(IGameLobby lobby) {
			lobby.getTrackingPlayers().sendPacket(LoveTropicsNetwork.CHANNEL, LobbyUpdateMessage.remove(lobby));
		}

		@Override
		public void onGamePhaseChange(IGameLobby lobby) {
			lobby.getTrackingPlayers().sendPacket(LoveTropicsNetwork.CHANNEL, LobbyUpdateMessage.update(lobby));
		}
	}
}
