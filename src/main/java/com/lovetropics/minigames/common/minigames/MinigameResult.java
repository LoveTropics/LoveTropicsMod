package com.lovetropics.minigames.common.minigames;

import net.minecraft.util.Unit;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Function;

public final class MinigameResult<T> {
	private static final MinigameResult<Unit> OK_UNIT = MinigameResult.ok(Unit.INSTANCE);

	private final T ok;
	private final ITextComponent error;

	private MinigameResult(T ok, ITextComponent error) {
		this.ok = ok;
		this.error = error;
	}

	public static <T> MinigameResult<T> ok(T ok) {
		return new MinigameResult<>(ok, null);
	}

	public static MinigameResult<Unit> ok() {
		return OK_UNIT;
	}

	public static <T> MinigameResult<T> error(ITextComponent error) {
		return new MinigameResult<>(null, error);
	}

	public static <T> MinigameResult<T> fromException(String message, Exception exception) {
		exception.printStackTrace();
		return MinigameResult.error(new StringTextComponent(message + ": " + exception.toString()));
	}

	public T getOk() {
		return ok;
	}

	public ITextComponent getError() {
		return error;
	}

	public boolean isOk() {
		return ok != null;
	}

	public boolean isError() {
		return error != null;
	}

	@SuppressWarnings("unchecked")
	public <U> MinigameResult<U> map(Function<T, U> function) {
		if (ok != null) {
			return MinigameResult.ok(function.apply(ok));
		} else {
			return (MinigameResult<U>) this;
		}
	}

	@SuppressWarnings("unchecked")
	public <U> MinigameResult<U> mapValue(U value) {
		if (ok != null) {
			return MinigameResult.ok(value);
		} else {
			return (MinigameResult<U>) this;
		}
	}

	@SuppressWarnings("unchecked")
	public <U> MinigameResult<U> castError() {
		if (error != null) {
			return (MinigameResult<U>) this;
		} else {
			throw new UnsupportedOperationException("not an error!");
		}
	}
}
