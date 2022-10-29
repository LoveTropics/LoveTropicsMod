package com.lovetropics.minigames.common.core.game.state;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public final class GamePhaseState implements IGameState {
	public static final GameStateKey<GamePhaseState> KEY = GameStateKey.create("Game Phases");

	// TODO: avoid stringly-typed phases?
	private GamePhase phase;
	private float progress;

	public GamePhaseState(GamePhase phase, float progress) {
		this.phase = phase;
		this.progress = progress;
	}

	public void set(@Nonnull GamePhase phase, float progress) {
		this.phase = phase;
		this.progress = progress;
	}

	@Nonnull
	public GamePhase get() {
		return phase;
	}

	public float progress() {
		return progress;
	}

	public boolean is(GamePhase phase) {
		return this.phase.equals(phase);
	}

	public boolean is(Predicate<GamePhase> predicate) {
		return predicate.test(this.phase);
	}
}
