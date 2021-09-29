package com.lovetropics.minigames.common.core.game.impl;

import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;

import javax.annotation.Nullable;

final class LobbyState {
	@Nullable
	private final GamePhase phase;
	private final LobbyControls controls;

	LobbyState(GamePhase phase, LobbyControls controls) {
		this.phase = phase;
		this.controls = controls;
	}

	@Nullable
	public GameInstance game() {
		return phase != null ? phase.game : null;
	}

	@Nullable
	public GamePhase phase() {
		return phase;
	}

	public LobbyControls controls() {
		return controls;
	}
}
