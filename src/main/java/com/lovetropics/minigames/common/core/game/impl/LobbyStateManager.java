package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.common.core.game.GamePhaseType;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;

import javax.annotation.Nullable;

final class LobbyStateManager {
	private final GameLobby lobby;
	private LobbyState state = new LobbyState.Paused();

	LobbyStateManager(GameLobby lobby) {
		this.lobby = lobby;
	}

	@Nullable
	public GameInstance getGame() {
		return state.phase != null ? state.phase.game : null;
	}

	@Nullable
	public GamePhase getPhase() {
		return state.phase;
	}

	@Nullable
	public ClientCurrentGame getClientCurrentGame() {
		return state.getClientCurrentGame();
	}

	public LobbyControls controls() {
		return state.controls;
	}

	@Nullable
	Change tick() {
		LobbyState newState = state.tick(lobby)
				.orElseGet(error -> errored(state, error));
		return this.trySetState(newState);
	}

	Change handleError(Component error) {
		LobbyState state = errored(this.state, error);
		return this.trySetState(state);
	}

	Change close() {
		return trySetState(new LobbyState.Closed());
	}

	@Nullable
	private Change trySetState(LobbyState newState) {
		LobbyState oldState = this.state;
		if (oldState == newState) return null;

		this.state = newState;
		return new Change(oldState.phase, newState.phase);
	}

	private LobbyState errored(LobbyState state, Component error) {
		LogManager.getLogger().info(error.getContents());
		GamePhase phase = state.phase;
		if (phase != null) {
			IGameDefinition definition = phase.getDefinition();
			GamePhaseType phaseType = phase.getPhaseType();
			return new LobbyState.Errored(definition, phaseType, error);
		} else {
			return new LobbyState.Paused();
		}
	}

	record Change(GamePhase oldPhase, GamePhase newPhase) {
	}
}
