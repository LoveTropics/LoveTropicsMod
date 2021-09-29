package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.common.core.game.GameResult;
import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.IGamePhaseDefinition;
import com.lovetropics.minigames.common.core.game.lobby.ILobbyGameQueue;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;
import com.lovetropics.minigames.common.core.game.lobby.QueuedGame;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

final class LobbyGameQueue implements ILobbyGameQueue {
	private final GameLobby lobby;
	private final Queue<QueuedGame> queue = new ArrayDeque<>();

	private State state;

	LobbyGameQueue(GameLobby lobby) {
		this.lobby = lobby;
	}

	@Override
	public QueuedGame enqueue(IGameDefinition game) {
		QueuedGame queued = QueuedGame.create(game);
		queue.add(queued);
		return queued;
	}

	@Override
	public void clear() {
		this.queue.clear();
	}

	@Override
	public boolean remove(QueuedGame game) {
		return queue.remove(game);
	}

	@Nullable
	public QueuedGame byNetworkId(int networkId) {
		for (QueuedGame game : queue) {
			if (game.networkId() == networkId) {
				return game;
			}
		}
		return null;
	}

	@Override
	public Iterator<QueuedGame> iterator() {
		return queue.iterator();
	}

	@Override
	public int size() {
		return queue.size();
	}

	void tryResume() {
		if (state != null) return;

		QueuedGame game = queue.peek();
		if (game != null) {
			CompletableFuture<GameResult<State>> future = createGame(game.definition());
			state = new State.Pending(null, future)
					.then(() -> queue.remove(game));
		}
	}

	@Nullable
	GameResult<LobbyState> tick(Supplier<LobbyState> pausedState) {
		State state = this.state;
		if (state == null) return null;

		GameResult<State> result = state.tick(this);
		if (result == null) {
			this.state = null;
			return GameResult.ok(pausedState.get());
		}

		if (result.isError()) {
			this.state = null;
			return result.castError();
		}

		State newState = result.getOk();
		if (newState != state) {
			this.state = newState;
			return GameResult.ok(new LobbyState(newState.phase, newState.controls));
		} else {
			return null;
		}
	}

	CompletableFuture<GameResult<State>> createGame(IGameDefinition definition) {
		GameInstance game = new GameInstance(lobby, definition);

		IGamePhaseDefinition playing = definition.getPlayingPhase();
		IGamePhaseDefinition waiting = definition.getWaitingPhase();
		if (waiting != null) {
			return this.createWaiting(game, waiting, playing);
		} else {
			return this.createPlaying(game, playing);
		}
	}

	CompletableFuture<GameResult<State>> createPlaying(GameInstance game, IGamePhaseDefinition definition) {
		return GamePhase.create(game, definition)
				.thenApply(result -> result.map(State.Playing::new));
	}

	CompletableFuture<GameResult<State>> createWaiting(GameInstance game, IGamePhaseDefinition definition, IGamePhaseDefinition playing) {
		return GamePhase.create(game, definition)
				.thenApply(result -> result.map(waiting -> {
					Supplier<State> start = () -> {
						CompletableFuture<GameResult<State>> next = createPlaying(waiting.game, playing);
						return new State.Pending(waiting, next);
					};
					return new State.Waiting(waiting, start);
				}));
	}

	static abstract class State {
		protected final GamePhase phase;
		protected final LobbyControls controls = new LobbyControls();

		protected State(@Nullable GamePhase phase) {
			this.phase = phase;
		}

		@Nullable
		protected abstract GameResult<State> tick(LobbyGameQueue queue);

		final Yield yield() {
			return new Yield(phase);
		}

		static final class Yield extends State {
			Yield(@Nullable GamePhase phase) {
				super(phase);
			}

			@Nullable
			@Override
			protected GameResult<State> tick(LobbyGameQueue queue) {
				QueuedGame game = queue.queue.peek();
				if (game == null) {
					return null;
				}

				Pending pending = new Pending(phase, queue.createGame(game.definition()))
						.then(() -> queue.remove(game));

				return GameResult.ok(pending);
			}
		}

		static final class Playing extends State {
			Playing(GamePhase phase) {
				super(phase);
				this.controls.add(LobbyControls.Type.SKIP, () -> phase.requestStop(GameStopReason.canceled()));
			}

			@Nullable
			@Override
			protected GameResult<State> tick(LobbyGameQueue queue) {
				GameStopReason stopping = phase.tick();
				if (stopping == null) {
					return GameResult.ok(this);
				} else {
					return nextState(stopping);
				}
			}

			private GameResult<State> nextState(GameStopReason stopping) {
				if (!stopping.isErrored()) {
					return GameResult.ok(this.yield());
				} else {
					return GameResult.error(stopping.getError());
				}
			}
		}

		static final class Waiting extends State {
			final Supplier<State> start;

			Waiting(GamePhase phase, Supplier<State> start) {
				super(phase);
				this.start = start;

				this.controls.add(LobbyControls.Type.PLAY, () -> phase.requestStop(GameStopReason.finished()));
				this.controls.add(LobbyControls.Type.SKIP, () -> phase.requestStop(GameStopReason.canceled()));
			}

			@Nullable
			@Override
			protected GameResult<State> tick(LobbyGameQueue queue) {
				GameStopReason stopping = phase.tick();
				if (stopping == null) {
					return GameResult.ok(this);
				} else {
					return nextState(stopping);
				}
			}

			private GameResult<State> nextState(GameStopReason stopping) {
				if (!stopping.isErrored()) {
					return GameResult.ok(stopping.isFinished() ? start.get() : this.yield());
				} else {
					return GameResult.error(stopping.getError());
				}
			}
		}

		static final class Pending extends State {
			final CompletableFuture<GameResult<State>> next;

			Pending(@Nullable GamePhase phase, CompletableFuture<GameResult<State>> next) {
				super(phase);
				this.next = next;
			}

			Pending then(Runnable runnable) {
				next.thenAccept($ -> runnable.run());
				return this;
			}

			@Nullable
			@Override
			protected GameResult<State> tick(LobbyGameQueue queue) {
				if (phase != null) {
					phase.tick();
				}
				return next.getNow(GameResult.ok(this));
			}
		}
	}
}
