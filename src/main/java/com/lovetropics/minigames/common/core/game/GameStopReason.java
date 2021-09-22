package com.lovetropics.minigames.common.core.game;

public enum GameStopReason {
	CANCELED,
	FINISHED,
	ERRORED;

	public boolean isFinished() {
		return this == FINISHED;
	}

	public boolean isCanceled() {
		return this == CANCELED || this == ERRORED;
	}
}
