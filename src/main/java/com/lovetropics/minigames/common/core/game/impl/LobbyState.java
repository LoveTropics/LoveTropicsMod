package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.event.GameWaitingEvents;
import com.lovetropics.minigames.common.core.game.config.WaitingLobbyConfig;
import com.lovetropics.minigames.common.core.game.lobby.QueuedGame;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.instances.control.ControlCommandInvoker;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Unit;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

// TODO: can we better compose this interface to not have a lot of methods to override?
interface LobbyState {
	default GameResult<Unit> requestStart() {
		return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_MINIGAME_ALREADY_STARTED));
	}

	default GameResult<Unit> requestStop() {
		return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME));
	}

	default GameResult<Unit> requestPause() {
		return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME));
	}

	default void onPlayerRegister(ServerPlayerEntity player, PlayerRole requestedRole) {
	}

	@Nullable
	default IActiveGame getActiveGame() {
		return null;
	}

	@Nullable
	default ControlCommandInvoker getControlCommands() {
		return ControlCommandInvoker.EMPTY;
	}

	default boolean isAccessible() {
		return false;
	}

	@Nullable
	LobbyState tick();

	// TODO: Name
	final class Factory {
		private final GameLobby lobby;

		Factory(GameLobby lobby) {
			this.lobby = lobby;
		}

		public LobbyState stopped() {
			return () -> null;
		}

		public LobbyState paused() {
			return new Paused(this::nextGame);
		}

		public LobbyState errored(ITextComponent error) {
			// TODO: use the error
			return this.paused();
		}

		@Nullable
		public LobbyState nextGame() {
			QueuedGame game = lobby.gameQueue.next();
			if (game == null) return null;

			WaitingLobbyConfig waitingLobby = game.definition().getWaitingLobby();
			if (waitingLobby != null) {
				return waitingLobby(game, waitingLobby);
			} else {
				return startGame(game);
			}
		}

		private LobbyState waitingLobby(QueuedGame game, WaitingLobbyConfig waitingLobby) {
			// TODO: use the waiting lobby map!
			ResourceLocation map = waitingLobby.map();

			return WaitingGame.create(lobby, game.behaviors())
					.map(waiting -> (LobbyState) new Waiting(waiting, () -> startGame(game), this::nextGame))
					.orElseGet(this::errored);
		}

		private LobbyState startGame(QueuedGame game) {
			CompletableFuture<LobbyState> future = this.startGameAsync(game)
					.thenApply(result ->
							result.map(active -> (LobbyState) new Active(active, this::nextGame))
									.orElseGet(this::errored)
					);
			return new Pending(future);
		}

		// TODO: extract this code somewhere else?
		private CompletableFuture<GameResult<ActiveGame>> startGameAsync(QueuedGame game) {
			CompletableFuture<GameResult<ActiveGame>> future = openMap(game).thenComposeAsync(map -> {
				if (map.isOk()) {
					return startGame(game, map.getOk());
				} else {
					return CompletableFuture.completedFuture(map.castError());
				}
			}, lobby.getServer());

			return GameResult.handleException(future, "Unknown exception starting game");
		}

		private CompletableFuture<GameResult<GameMap>> openMap(QueuedGame game) {
			return game.definition().getMap().open(lobby.getServer());
		}

		private CompletableFuture<GameResult<ActiveGame>> startGame(QueuedGame game, GameMap map) {
			// TODO: if a player registers after this point they won't be considered at all to join as a spectator
			List<ServerPlayerEntity> participants = new ArrayList<>();
			List<ServerPlayerEntity> spectators = new ArrayList<>();
			lobby.collectRegistrations(participants, spectators, game.definition());

			return ActiveGame.start(lobby, game.definition(), map, game.behaviors(), participants, spectators);
		}
	}

	final class Paused implements LobbyState {
		private final Supplier<LobbyState> start;
		private boolean started;

		Paused(Supplier<LobbyState> start) {
			this.start = start;
		}

		@Override
		public GameResult<Unit> requestStart() {
			started = true;
			return GameResult.ok();
		}

		@Override
		public GameResult<Unit> requestStop() {
			return requestStart();
		}

		@Override
		public LobbyState tick() {
			return started ? start.get() : this;
		}
	}

	final class Waiting implements LobbyState {
		private final WaitingGame waiting;
		private final Supplier<LobbyState> start;
		private final Supplier<LobbyState> skip;

		private boolean started;
		private boolean stopped;

		Waiting(WaitingGame waiting, Supplier<LobbyState> start, Supplier<LobbyState> skip) {
			this.waiting = waiting;
			this.start = start;
			this.skip = skip;

			// TODO: we need to call the player waiting listener for waiting players: we probably need a start/stop function in the state
		}

		@Override
		public GameResult<Unit> requestStart() {
			started = true;
			return GameResult.ok();
		}

		@Override
		public GameResult<Unit> requestStop() {
			stopped = true;
			return GameResult.ok();
		}

		@Override
		public void onPlayerRegister(ServerPlayerEntity player, PlayerRole requestedRole) {
			try {
				waiting.invoker(GameWaitingEvents.PLAYER_WAITING).onPlayerWaiting(waiting, player, requestedRole);
			} catch (Exception e) {
				LoveTropics.LOGGER.warn("Failed to dispatch player register event", e);
			}
		}

		@Nullable
		@Override
		public ControlCommandInvoker getControlCommands() {
			return waiting.getControlCommands();
		}

		@Override
		public boolean isAccessible() {
			return true;
		}

		@Override
		public LobbyState tick() {
			if (started) return start.get();
			if (stopped) return skip.get();
			return this;
		}
	}

	final class Pending implements LobbyState {
		private final CompletableFuture<LobbyState> next;

		Pending(CompletableFuture<LobbyState> next) {
			this.next = next;
		}

		@Override
		@Nullable
		public LobbyState tick() {
			return next.getNow(this);
		}
	}

	final class Active implements LobbyState {
		private final ActiveGame game;
		private final Supplier<LobbyState> next;

		private boolean stopped;
		private boolean paused;

		Active(ActiveGame game, Supplier<LobbyState> next) {
			this.game = game;
			this.next = next;
		}

		@Override
		public void onPlayerRegister(ServerPlayerEntity player, PlayerRole requestedRole) {
			game.addPlayerTo(player, PlayerRole.SPECTATOR);
		}

		@Override
		public GameResult<Unit> requestStop() {
			stopped = true;
			return GameResult.ok();
		}

		@Override
		public GameResult<Unit> requestPause() {
			paused = true;
			return GameResult.ok();
		}

		@Override
		public boolean isAccessible() {
			return true;
		}

		@Nullable
		@Override
		public IActiveGame getActiveGame() {
			return game;
		}

		@Nullable
		@Override
		public ControlCommandInvoker getControlCommands() {
			return game.getControlCommands();
		}

		@Override
		@Nullable
		public LobbyState tick() {
			if (stopped) {
				game.stop(GameStopReason.CANCELED);
				return next.get();
			} else if (paused) {
				return new Paused(() -> this);
			}

			return game.tick() ? this : next.get();
		}
	}
}
