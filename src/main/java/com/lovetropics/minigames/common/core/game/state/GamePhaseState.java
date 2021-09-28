package com.lovetropics.minigames.common.core.game.state;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public final class GamePhaseState implements IGameState {
	public static final GameStateKey<GamePhaseState> KEY = GameStateKey.create("Game Phases");

	// TODO: avoid stringly-typed phases?
	private GamePhase phase;

	public GamePhaseState(GamePhase phase) {
		this.phase = phase;
	}

	public void set(@Nonnull GamePhase phase) {
		this.phase = phase;
	}

	@Nonnull
	public GamePhase get() {
		return phase;
	}

	public boolean is(String phase) {
		return this.phase.is(phase);
	}

	public boolean is(Predicate<GamePhase> predicate) {
		return predicate.test(this.phase);
	}
}
