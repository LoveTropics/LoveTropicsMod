package com.lovetropics.minigames.common.core.game.impl;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.client.lobby.state.message.JoinedLobbyMessage;
import com.lovetropics.minigames.client.lobby.state.message.LeftLobbyMessage;
import com.lovetropics.minigames.client.lobby.state.message.LobbyPlayersMessage;
import com.lovetropics.minigames.client.lobby.state.message.LobbyUpdateMessage;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.lobby.*;
import com.lovetropics.minigames.common.core.game.player.PlayerIterable;
import com.lovetropics.minigames.common.core.game.player.PlayerRoleSelections;
import com.lovetropics.minigames.common.core.game.util.GameTexts;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;

// TODO: do we want a different game lobby implementation for something like carnival games?
final class GameLobby implements IGameLobby {
	final MultiGameManager manager;
	final MinecraftServer server;
	GameLobbyMetadata metadata;

	final LobbyGameQueue gameQueue;
	final LobbyStateManager state;
	final LobbyPlayerManager players;
	final LobbyManagement management;
	final LobbyTrackingPlayers trackingPlayers;

	final LobbyStateListener stateListener = LobbyStateListener.compose(
			new NetworkUpdateListener(),
			new ChatNotifyListener()
	);

	private boolean closed;

	GameLobby(MultiGameManager manager, MinecraftServer server, GameLobbyMetadata metadata) {
		this.manager = manager;
		this.server = server;
		this.metadata = metadata;

		this.gameQueue = new LobbyGameQueue(server);
		this.state = new LobbyStateManager(this);
		this.players = new LobbyPlayerManager(this);
		this.management = new LobbyManagement(this);
		this.trackingPlayers = new LobbyTrackingPlayers(this);
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
		return state.getPhase();
	}

	@Nullable
	@Override
	public ClientCurrentGame getClientCurrentGame() {
		return state.getClientCurrentGame();
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
	public boolean isVisibleTo(CommandSourceStack source) {
		if (management.canManage(source)) {
			return true;
		}

		return metadata.visibility().isPublic();
	}

	void setName(String name) {
		metadata = metadata.withName(name);
		stateListener.onLobbyNameChange(this);
	}

	void setVisibility(LobbyVisibility visibility) {
		metadata = manager.setVisibility(this, visibility);
		this.trackingPlayers.rebuildTracking();
	}

	void tick() {
		LobbyStateManager.Change change = state.tick();
		if (change != null) {
			GameResult<Unit> result = onStateChange(change);
			if (result.isError()) {
				onStateChange(state.handleError(result.getError()));
			}
		}
	}

	private GameResult<Unit> onStateChange(LobbyStateManager.Change change) {
		GamePhase oldPhase = change.oldPhase;
		GamePhase newPhase = change.newPhase;
		if (newPhase != oldPhase) {
			GameResult<Unit> result = onGamePhaseChange(oldPhase, newPhase);
			if (result.isError()) {
				return result;
			}
		}

		management.onGameStateChange();
		stateListener.onLobbyStateChange(this);

		return GameResult.ok();
	}

	private GameResult<Unit> onGamePhaseChange(GamePhase oldPhase, GamePhase newPhase) {
		GameResult<Unit> result = GameResult.ok();

		if (newPhase == null && oldPhase != null) {
			onQueuePaused();
		}

		if (oldPhase != null) {
			oldPhase.destroy();
			manager.removeGamePhaseFromDimension(oldPhase.getDimension(), oldPhase);
		}

		if (newPhase != null) {
			manager.addGamePhaseToDimension(newPhase.getDimension(), newPhase);
			result = newPhase.start();
		}

		GameInstance oldGame = oldPhase != null ? oldPhase.game : null;
		GameInstance newGame = newPhase != null ? newPhase.game : null;
		if (oldGame != newGame) {
			onGameInstanceChange(oldGame, newGame);
		}

		stateListener.onGamePhaseChange(this);

		return result;
	}

	private void onGameInstanceChange(GameInstance oldGame, GameInstance newGame) {
		if (newGame != null) {
			onGameInstanceStart(newGame);
		}
	}

	private void onGameInstanceStart(GameInstance game) {
		IGameDefinition definition = game.getDefinition();
		if (definition.getWaitingPhase().isPresent()) {
			PlayerRoleSelections roleSelections = players.getRoleSelections();
			roleSelections.clearAndPromptAll(players);
		}
	}

	void onQueuePaused() {
		for (ServerPlayer player : getPlayers()) {
			onPlayerExitGame(player);
		}

		stateListener.onLobbyPaused(this);
	}

	void onPlayerLoggedIn(ServerPlayer player) {
		trackingPlayers.onPlayerLoggedIn(player);
	}

	void onPlayerLoggedOut(ServerPlayer player) {
		trackingPlayers.onPlayerLoggedOut(player);

		players.remove(player);
	}

	void onPlayerRegister(ServerPlayer player) {
		manager.addPlayerToLobby(player, this);

		stateListener.onPlayerJoin(this, player);

		GamePhase phase = state.getPhase();
		if (phase != null) {
			phase.onPlayerJoin(player);
		}

		management.onPlayersChanged();
	}

	void onPlayerLeave(ServerPlayer player) {
		GamePhase phase = state.getPhase();
		if (phase != null) {
			player = phase.onPlayerLeave(player);
		}

		stateListener.onPlayerLeave(this, player);
		management.stopManaging(player);

		management.onPlayersChanged();

		manager.removePlayerFromLobby(player, this);
	}

	// TODO: better abstract this logic?
	void onPlayerExitGame(ServerPlayer player) {
		PlayerIsolation.INSTANCE.restore(player);
	}

	void onPlayerStartTracking(ServerPlayer player) {
		stateListener.onPlayerStartTracking(this, player);
	}

	void onPlayerStopTracking(ServerPlayer player) {
		stateListener.onPlayerStopTracking(this, player);
	}

	void close() {
		if (closed) return;
		closed = true;

		try {
			management.disable();
			onStateChange(state.close());

			LobbyPlayerManager players = getPlayers();
			for (ServerPlayer player : Lists.newArrayList(players)) {
				players.remove(player);
			}

			stateListener.onLobbyStop(this);
			gameQueue.clear();
		} finally {
			manager.removeLobby(this);
		}
	}

	static final class ChatNotifyListener implements LobbyStateListener {
		@Override
		public void onPlayerJoin(IGameLobby lobby, ServerPlayer player) {
			IGame currentGame = lobby.getCurrentGame();
			if (currentGame != null) {
				onPlayerJoinGame(lobby, currentGame);
			}
		}

		@Override
		public void onPlayerLeave(IGameLobby lobby, ServerPlayer player) {
			IGame currentGame = lobby.getCurrentGame();
			if (currentGame != null) {
				onPlayerLeaveGame(lobby, currentGame);
			}
		}

		@Override
		public void onPlayerStartTracking(IGameLobby lobby, ServerPlayer player) {
			player.displayClientMessage(GameTexts.Status.lobbyOpened(lobby), false);
		}

		private void onPlayerJoinGame(IGameLobby lobby, IGame currentGame) {
			int minimumParticipants = currentGame.getDefinition().getMinimumParticipantCount();
			if (lobby.getPlayers().size() == minimumParticipants) {
				Component enoughPlayers = GameTexts.Status.enoughPlayers();
				lobby.getTrackingPlayers().sendMessage(enoughPlayers);
			}
		}

		private void onPlayerLeaveGame(IGameLobby lobby, IGame currentGame) {
			int minimumParticipants = currentGame.getDefinition().getMinimumParticipantCount();
			if (lobby.getPlayers().size() == minimumParticipants - 1) {
				Component noLongerEnoughPlayers = GameTexts.Status.noLongerEnoughPlayers();
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
		public void onPlayerJoin(IGameLobby lobby, ServerPlayer player) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), JoinedLobbyMessage.create(lobby));
			lobby.getTrackingPlayers().sendPacket(LoveTropicsNetwork.CHANNEL, LobbyPlayersMessage.update(lobby));
		}

		@Override
		public void onPlayerLeave(IGameLobby lobby, ServerPlayer player) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new LeftLobbyMessage());
			lobby.getTrackingPlayers().sendPacket(LoveTropicsNetwork.CHANNEL, LobbyPlayersMessage.update(lobby));
		}

		@Override
		public void onPlayerStartTracking(IGameLobby lobby, ServerPlayer player) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), LobbyUpdateMessage.update(lobby));
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), LobbyPlayersMessage.update(lobby));
		}

		@Override
		public void onPlayerStopTracking(IGameLobby lobby, ServerPlayer player) {
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), LobbyUpdateMessage.remove(lobby));
		}

		@Override
		public void onLobbyStateChange(IGameLobby lobby) {
			lobby.getTrackingPlayers().sendPacket(LoveTropicsNetwork.CHANNEL, LobbyUpdateMessage.update(lobby));
		}

		@Override
		public void onLobbyNameChange(IGameLobby lobby) {
			lobby.getTrackingPlayers().sendPacket(LoveTropicsNetwork.CHANNEL, LobbyUpdateMessage.update(lobby));
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
