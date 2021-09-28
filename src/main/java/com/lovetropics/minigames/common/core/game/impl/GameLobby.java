package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.client.lobby.state.message.LobbyUpdateMessage;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.lobby.*;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

// TODO: do we want a different game lobby implementation for something like carnival games?
final class GameLobby implements IGameLobby {
	final MultiGameManager manager;
	final MinecraftServer server;
	GameLobbyMetadata metadata;

	final LobbyPlayerManager players;

	final LobbyVisibility visibility = LobbyVisibility.PRIVATE;

	final LobbyGameQueue gameQueue = new LobbyGameQueue();

	final LobbyWatcher watcher = LobbyWatcher.compose(new LobbyWatcher.Network(), new LobbyWatcher.Messages());
	final LobbyManagement management;

	GameInstance currentGame;
	boolean paused = true;

	GameLobby(MultiGameManager manager, MinecraftServer server, GameLobbyMetadata metadata) {
		this.manager = manager;
		this.server = server;
		this.metadata = metadata;

		this.players = new LobbyPlayerManager(this);
		this.management = new LobbyManagement(this);

		// TODO: move out of constructor
		this.watcher.onLobbyCreate(this);
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
	public GameInstance getCurrentGame() {
		return currentGame;
	}

	@Override
	public LobbyControls getControls() {
		if (this.paused) {
			return new LobbyControls()
					.add(LobbyControls.Type.PLAY, () -> {
						this.paused = false;
						onGameStateChange();
						return GameResult.ok();
					});
		}

		GameInstance currentGame = this.currentGame;
		return currentGame != null ? currentGame.getControls() : LobbyControls.empty();
	}

	@Override
	public ILobbyManagement getManagement() {
		return management;
	}

	// TODO: publish state to all tracking players when visibility changes
	@Override
	public boolean isVisibleTo(CommandSource source) {
		if (management.canManage(source)) {
			return true;
		}

		return currentGame != null && visibility.isPublic();
	}

	void setName(String name) {
		metadata = manager.renameLobby(metadata, name);
	}

	boolean tick() {
		GameInstance currentGame = this.currentGame;
		if (currentGame != null) {
			tickPlaying(currentGame);
		} else {
			tickInactive();
		}

		return true;
	}

	private void tickPlaying(GameInstance game) {
		if (!game.tick()) {
			tryMoveToNextGame();
		}
	}

	private void tickInactive() {
		if (!paused) {
			tryMoveToNextGame();
		}
	}

	private void tryMoveToNextGame() {
		GameInstance next = nextGame();
		currentGame = next;
		paused |= next == null;

		onGameStateChange();
	}

	@Nullable
	private GameInstance nextGame() {
		if (paused) return null;

		QueuedGame game = gameQueue.next();
		if (game == null) return null;

		return new GameInstance(this, game.definition());
	}

	void onGameStateChange() {
		management.updateControlsState();

		// TODO: check where we send this & move into watcher
		LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), LobbyUpdateMessage.update(this));
	}

	void cancel() {
		GameInstance game = this.currentGame;
		this.currentGame = null;

		if (game != null) {
			game.cancel();
		}

		this.onStopped();
	}

	void onStopped() {
		watcher.onLobbyStop(this);
		gameQueue.clear();
		currentGame = null;
		paused = true;

		manager.removeLobby(this);
	}

	void onPlayerRegister(ServerPlayerEntity player, PlayerRole requestedRole) {
		manager.addPlayerToLobby(player, this);

		GameInstance currentGame = this.currentGame;
		if (currentGame != null) {
			currentGame.onPlayerJoin(player);
		}

		// TODO: setting roles within the active game must also send update packets, but we don't want to duplicate
		PlayerRole trueRole = requestedRole != null ? requestedRole : PlayerRole.PARTICIPANT;
		watcher.onPlayerJoin(this, player, trueRole);
	}

	void onPlayerLeave(ServerPlayerEntity player) {
		GameInstance currentGame = this.currentGame;
		if (currentGame != null) {
			currentGame.onPlayerLeave(player);
		}

		watcher.onPlayerLeave(this, player);
		management.stopManaging(player);

		manager.removePlayerFromLobby(player, this);
	}

	void onPhaseStart(GamePhase game) {
		manager.addGamePhaseToDimension(game.getDimension(), game);
	}

	void onPhaseStop(GamePhase game) {
		manager.removeGamePhaseFromDimension(game.getDimension(), game);
	}
}
