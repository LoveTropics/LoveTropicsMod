package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.client.lobby.state.message.LobbyUpdateMessage;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.lobby.*;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

import javax.annotation.Nullable;

// TODO: do we want a different game lobby implementation for something like carnival games?
final class GameLobby implements IGameLobby {
	final MultiGameManager manager;
	final MinecraftServer server;
	final GameLobbyMetadata metadata;

	final LobbyPlayerManager players;

	final LobbyVisibility visibility = LobbyVisibility.PRIVATE;

	final LobbyGameQueue gameQueue = new LobbyGameQueue();

	final LobbyWatcher watcher = LobbyWatcher.compose(new LobbyWatcher.Network(), new LobbyWatcher.Messages());

	GameInstance currentGame;
	boolean paused;

	GameLobby(MultiGameManager manager, MinecraftServer server, GameLobbyMetadata metadata) {
		this.manager = manager;
		this.server = server;
		this.metadata = metadata;

		this.players = new LobbyPlayerManager(this);

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

	// TODO: publish state to all tracking players when visibility changes
	@Override
	public boolean isVisibleTo(CommandSource source) {
		if (source.hasPermissionLevel(2) || metadata.initiator().matches(source.getEntity())) {
			return true;
		}

		return currentGame != null && visibility.isPublic();
	}

	boolean tick() {
		GameInstance currentGame = this.currentGame;
		if (currentGame != null) {
			tickPlaying(currentGame);
		}

		return true;
	}

	private void tickPlaying(GameInstance game) {
		if (!game.tick()) {
			GameInstance next = nextGame();
			currentGame = next;
			paused |= next == null;

			// TODO: check where we send this & move into watcher
			LoveTropicsNetwork.CHANNEL.send(PacketDistributor.ALL.noArg(), LobbyUpdateMessage.update(this));
		}
	}

	@Nullable
	private GameInstance nextGame() {
		if (paused) return null;

		QueuedGame game = gameQueue.next();
		if (game == null) return null;

		return new GameInstance(this, game.definition());
	}

	void cancel() {
		GameInstance game = this.currentGame;
		this.currentGame = null;

		if (game != null) {
			game.stop(GameStopReason.CANCELED);
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

	@Override
	public GameResult<Unit> requestStart() {
		GameInstance currentGame = this.currentGame;
		if (currentGame == null) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME));
		}

		return currentGame.requestStart();
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

		manager.removePlayerFromLobby(player, this);
	}

	void onPhaseStart(GamePhase game) {
		manager.addGamePhaseToDimension(game.getDimension(), game);
	}

	void onPhaseStop(GamePhase game) {
		manager.removeGamePhaseFromDimension(game.getDimension(), game);
	}
}
