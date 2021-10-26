package com.lovetropics.minigames.common.core.game;

import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;

public final class GameStopReason {
	private static final GameStopReason FINISHED = new GameStopReason(true, null);
	private static final GameStopReason CANCELED = new GameStopReason(false, null);

	private final boolean finished;
	@Nullable
	private final ITextComponent error;

	private GameStopReason(boolean finished, @Nullable ITextComponent error) {
		this.finished = finished;
		this.error = error;
	}

	public static GameStopReason finished() {
		return FINISHED;
	}

	public static GameStopReason canceled() {
		return CANCELED;
	}

	public static GameStopReason errored(ITextComponent error) {
		return new GameStopReason(false, error);
	}

	public boolean isFinished() {
		return finished;
	}

	public boolean isCanceled() {
		return !finished;
	}

	public boolean isErrored() {
		return error != null;
	}

	@Nullable
	public ITextComponent getError() {
		return error;
	}
}
