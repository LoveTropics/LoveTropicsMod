package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePollingEvents;
import com.lovetropics.minigames.common.core.game.map.GameMap;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.instances.control.ControlCommandInvoker;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Unit;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

interface LobbyState {
	default GameResult<Unit> requestStart() {
		return GameResult.error(new StringTextComponent(LoveTropicsLangKeys.COMMAND_MINIGAME_ALREADY_STARTED));
	}

	// TODO: all these functions aren't that nice
	default void registerPlayer(ServerPlayerEntity player, PlayerRole requestedRole) {
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
		// TODO: circular reference
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

		public LobbyState nextGame() {
			IGameDefinition definition = lobby.gameQueue.next();
			if (definition == null) return null;

			GameResult<WaitingGame> waitingResult = WaitingGame.create(lobby, definition);
			if (waitingResult.isError()) {
				// TODO: do something with the error
				return paused();
			}

			WaitingGame waiting = waitingResult.getOk();
			return new Waiting(waiting, () -> startGame(definition, waiting));
		}

		public LobbyState startGame(IGameDefinition definition, WaitingGame waiting) {
			// TODO: waiting lobby if the game wants it
			CompletableFuture<LobbyState> future = this.tryStartGame(definition, waiting)
					.thenApply(result -> result.<LobbyState>map(game -> new Active(game, this::nextGame)))
					.thenApply(result -> {
						// TODO: do something with the error
						return result.orElseGet(error -> paused());
					});

			return new Pending(future);
		}

		// TODO: this code is all cursed
		private CompletableFuture<GameResult<ActiveGame>> tryStartGame(IGameDefinition definition, WaitingGame waiting) {
			return definition.getMap().open(lobby.getServer())
					.thenComposeAsync(map -> {
						if (map.isOk()) {
							return intoActive(definition, waiting, map.getOk());
						} else {
							return CompletableFuture.completedFuture(map.castError());
						}
					}, lobby.getServer())
					.handle((result, throwable) -> {
						if (throwable instanceof Exception) {
							return GameResult.fromException("Unknown error starting game", (Exception) throwable);
						}
						return result;
					});
		}

		private CompletableFuture<GameResult<ActiveGame>> intoActive(IGameDefinition definition, WaitingGame waiting, GameMap map) {
			List<ServerPlayerEntity> participants = new ArrayList<>();
			List<ServerPlayerEntity> spectators = new ArrayList<>();

			lobby.registrations.collectInto(lobby.getServer(), participants, spectators, definition.getMaximumParticipantCount());

			return waiting.intoActive(map, participants, spectators);
		}
	}

	final class Paused implements LobbyState {
		private final Supplier<LobbyState> start;
		private boolean started;

		Paused(Supplier<LobbyState> start) {
			this.start = start;
		}

		// TODO: split controls for start / unpause?
		@Override
		public GameResult<Unit> requestStart() {
			started = true;
			return GameResult.ok();
		}

		@Override
		public LobbyState tick() {
			return started ? start.get() : this;
		}
	}

	final class Waiting implements LobbyState {
		private final WaitingGame waiting;
		private final Supplier<LobbyState> start;
		private boolean started;

		Waiting(WaitingGame waiting, Supplier<LobbyState> start) {
			this.waiting = waiting;
			this.start = start;
		}

		@Override
		public GameResult<Unit> requestStart() {
			started = true;
			return GameResult.ok();
		}

		@Override
		public void registerPlayer(ServerPlayerEntity player, PlayerRole requestedRole) {
			try {
				waiting.invoker(GamePollingEvents.PLAYER_REGISTER).onPlayerRegister(waiting, player, requestedRole);
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
			return started ? start.get() : this;
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

		Active(ActiveGame game, Supplier<LobbyState> next) {
			this.game = game;
			this.next = next;
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
			return game.tick() ? this : next.get();
		}
	}
}
