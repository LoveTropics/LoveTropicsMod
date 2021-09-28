package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

final class GameInstance implements IGameInstance {
	final GameLobby lobby;
	final MinecraftServer server;
	final IGameDefinition definition;

	final GameStateMap stateMap = new GameStateMap();

	private State state;

	GameInstance(GameLobby lobby, IGameDefinition definition) {
		this.lobby = lobby;
		this.server = lobby.getServer();
		this.definition = definition;

		this.state = initState();
	}

	private State initState() {
		IGamePhaseDefinition playingPhase = definition.getPlayingPhase();
		IGamePhaseDefinition waitingPhase = definition.getWaitingPhase();
		return waitingPhase != null ? waiting(playingPhase, waitingPhase) : playing(playingPhase);
	}

	private State waiting(IGamePhaseDefinition playingDefinition, IGamePhaseDefinition waitingDefinition) {
		return startPhase(waitingDefinition, phase -> {
			Supplier<State> start = () -> playing(playingDefinition);
			return new Waiting(phase, start);
		});
	}

	private State playing(IGamePhaseDefinition playingPhase) {
		return startPhase(playingPhase, Playing::new);
	}

	private State startPhase(IGamePhaseDefinition definition, Function<GamePhase, State> stateFactory) {
		CompletableFuture<State> future = GamePhase.start(this, definition)
				.thenApply(result -> result.map(stateFactory).orElseGet(this::errored));

		return new Pending(future);
	}

	private State errored(ITextComponent error) {
		// TODO
		return null;
	}

	@Override
	public IGameLobby getLobby() {
		return lobby;
	}

	@Override
	public IGameDefinition getDefinition() {
		return definition;
	}

	@Override
	public GameStateMap getState() {
		return stateMap;
	}

	@Override
	@Nullable
	public GamePhase getCurrentPhase() {
		State state = this.state;
		return state != null ? state.phase : null;
	}

	public LobbyControls getControls() {
		State state = this.state;
		return state != null ? state.controls : LobbyControls.empty();
	}

	void cancel() {
		GamePhase phase = getCurrentPhase();
		if (phase != null) {
			state = null;
			phase.stop(GameStopReason.CANCELED);
		}
	}

	boolean tick() {
		State state = this.state;
		if (state != null) {
			State newState = state.tick();
			this.state = newState;
			return newState != null;
		}
		return false;
	}

	void onPlayerJoin(ServerPlayerEntity player) {
		GamePhase phase = getCurrentPhase();
		if (phase != null) {
			phase.onPlayerJoin(player);
		}
	}

	void onPlayerLeave(ServerPlayerEntity player) {
		GamePhase phase = getCurrentPhase();
		if (phase != null) {
			phase.onPlayerLeave(player);
		}
	}

	void onPhaseStart(GamePhase phase) {
		lobby.onPhaseStart(phase);
	}

	void onPhaseStop(GamePhase phase) {
		lobby.onPhaseStop(phase);
	}

	static abstract class State {
		final GamePhase phase;
		final LobbyControls controls = new LobbyControls();

		protected State(GamePhase phase) {
			this.phase = phase;
		}

		@Nullable
		abstract State tick();

		static State finish() {
			return null;
		}
	}

	static final class Pending extends State {
		private final CompletableFuture<State> next;

		Pending(CompletableFuture<State> next) {
			super(null);
			this.next = next;
		}

		@Override
		public State tick() {
			return this.next.getNow(this);
		}
	}

	static final class Playing extends State {
		Playing(GamePhase phase) {
			super(phase);
			this.controls.add(LobbyControls.Type.STOP, () -> phase.stop(GameStopReason.CANCELED));
		}

		@Nullable
		@Override
		public State tick() {
			return phase.tick() ? this : finish();
		}
	}

	static final class Waiting extends State {
		private final Supplier<State> start;
		private boolean started;

		Waiting(GamePhase phase, Supplier<State> start) {
			super(phase);
			this.start = start;

			this.controls.add(LobbyControls.Type.PLAY, () -> {
				started = true;
				return GameResult.ok();
			});
			this.controls.add(LobbyControls.Type.STOP, () -> phase.stop(GameStopReason.CANCELED));
		}

		@Nullable
		@Override
		public State tick() {
			if (started) {
				return start.get();
			}

			return phase.tick() ? this : finish();
		}
	}
}
