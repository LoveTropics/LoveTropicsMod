package com.lovetropics.minigames.common.core.game.state;

import net.minecraft.util.Mth;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public final class GamePhaseState implements IGameState {
	public static final GameStateKey<GamePhaseState> KEY = GameStateKey.create("Game Phases");

	public static final int NO_TIME_ESTIMATE = -1;

	// TODO: avoid stringly-typed phases?
	private GamePhase phase;
	private int phaseLength = NO_TIME_ESTIMATE;
	private float progress;

	public GamePhaseState(GamePhase phase, float progress) {
		this.phase = phase;
		this.progress = progress;
	}

	public void set(@Nonnull GamePhase phase, int length) {
		this.phase = phase;
		this.phaseLength = length;
		this.progress = 0.0f;
	}

	public void set(@Nonnull GamePhase phase) {
		set(phase, NO_TIME_ESTIMATE);
	}

	public void update(float progress) {
		this.progress = progress;
	}

	@Nonnull
	public GamePhase get() {
		return phase;
	}

	public float progress() {
		return progress;
	}

	public int ticksLeft() {
		if (phaseLength == NO_TIME_ESTIMATE) {
			return NO_TIME_ESTIMATE;
		}
		return Mth.floor((1.0f - progress) * phaseLength);
	}

	public boolean is(GamePhase phase) {
		return this.phase.equals(phase);
	}

	public boolean is(Predicate<GamePhase> predicate) {
		return predicate.test(this.phase);
	}
}
