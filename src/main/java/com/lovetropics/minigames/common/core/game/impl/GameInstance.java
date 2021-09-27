package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.client.data.LoveTropicsLangKeys;
import com.lovetropics.minigames.common.core.game.*;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControl;
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
		return startPhase(playingPhase, () -> null);
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
		return state != null ? state.getCurrentPhase() : null;
	}

	GameResult<Unit> requestStart() {
		State state = this.state;
		if (state == null) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME));
		}

		LobbyControl control = state.getControl(LobbyControl.Type.PLAY);
		if (control == null) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME));
		}

		return control.run();
	}

	@Override
	public GameResult<Unit> stop(GameStopReason reason) {
		GamePhase phase = getCurrentPhase();
		if (phase == null) {
			return GameResult.error(new TranslationTextComponent(LoveTropicsLangKeys.COMMAND_NO_MINIGAME));
		}

		state = null;

		// TODO: update message
		PlayerSet.ofServer(server).sendMessage(GameMessages.forLobby(lobby).finished());

		return phase.stop(reason);
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

	interface State {
		@Nullable
		default LobbyControl getControl(LobbyControl.Type type) {
			return null;
		}

		@Nullable
		default GamePhase getCurrentPhase() {
			return null;
		}

		@Nullable
		State tick();
	}

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

		Playing(GamePhase phase, Supplier<State> next) {
			this.phase = phase;
			this.next = next;
		}

		// TODO: getters might not be the best way to handle this
		@Nullable
		@Override
		public LobbyControl getControl(LobbyControl.Type type) {
			switch (type) {
				case PLAY: return () -> {
					started = true; // TODO: different when waiting lobby
					return GameResult.ok();
				};
				case STOP: return () -> phase.stop(GameStopReason.CANCELED);
				default: return null;
			}
		}

		@Override
		public GamePhase getCurrentPhase() {
			return phase;
		}

		@Nullable
		@Override
		public State tick() {
			if (started) return next.get();

			return phase.tick() ? this : null;
		}
	}
}
