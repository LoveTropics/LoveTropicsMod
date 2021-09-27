package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.util.GameMessages;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
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

	private State waiting(IGamePhaseDefinition playingPhase, IGamePhaseDefinition waitingPhase) {
		return startPhase(waitingPhase, () -> playing(playingPhase));
	}

	private State playing(IGamePhaseDefinition playingPhase) {
		return startPhase(playingPhase, Finished::new);
	}

	private State startPhase(IGamePhaseDefinition phaseDefinition, Supplier<State> next) {
		CompletableFuture<State> future = GamePhase.start(this, phaseDefinition)
				.thenApply(result ->
						result.map(phase -> (State) new Playing(phase, next))
								.orElseGet(this::errored)
				);
		return new Pending(future);
	}

	private State errored(ITextComponent error) {
		// TODO
		return new Finished();
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
		return state.getCurrentPhase();
	}

	@Override
	public GameResult<Unit> stop(GameStopReason reason) {
		GamePhase phase = state.getCurrentPhase();
		if (phase == null) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME));
		}

		state = new Finished();

		// TODO: update message
		PlayerSet.ofServer(server).sendMessage(GameMessages.forLobby(lobby).finished());

		return phase.stop(reason);
	}

	boolean tick() {
		state = state.tick();
		return state != null;
	}

	void onPlayerJoin(ServerPlayerEntity player) {
		GamePhase phase = state.getCurrentPhase();
		if (phase != null) {
			phase.onPlayerJoin(player);
		}
	}

	void onPlayerLeave(ServerPlayerEntity player) {
		GamePhase phase = state.getCurrentPhase();
		if (phase != null) {
			phase.onPlayerLeave(player);
		}
	}

	GameResult<Unit> requestStart() {
		return state.requestStart();
	}

	GameResult<Unit> requestStop() {
		return state.requestStop();
	}

	// TODO: we maybe don't need a state interface
	interface State {
		default GameResult<Unit> requestStart() {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_MINIGAME_ALREADY_STARTED));
		}

		default GameResult<Unit> requestStop() {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME));
		}

		@Nullable
		default GamePhase getCurrentPhase() {
			return null;
		}

		@Nullable
		State tick();
	}

	// TODO: pending must still be able to delegate to inner state
	static final class Pending implements State {
		private final CompletableFuture<State> next;

		Pending(CompletableFuture<State> next) {
			this.next = next;
		}

		@Override
		public State tick() {
			return this.next.getNow(this);
		}
	}

	static final class Playing implements State {
		private final GamePhase phase;
		private final Supplier<State> next;

		private boolean started;
		private boolean stopped;

		Playing(GamePhase phase, Supplier<State> next) {
			this.phase = phase;
			this.next = next;
		}

		@Override
		public GameResult<Unit> requestStart() {
			started = true;
			return GameResult.ok();
		}

		@Override
		public GameResult<Unit> requestStop() {
			stopped = true; // TODO: invoke stop on phase
			return GameResult.ok();
		}

		@Override
		public GamePhase getCurrentPhase() {
			return phase;
		}

		@Nullable
		@Override
		public State tick() {
			if (started) return next.get();
			else if (stopped) return null;

			return phase.tick() ? this : null;
		}
	}

	static final class Finished implements State {
		@Nullable
		@Override
		public State tick() {
			return null;
		}
	}
}
