package com.lovetropics.minigames.common.core.game;

import net.minecraft.util.Unit;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Function;

public final class GameResult<T> {
	private static final GameResult<Unit> OK_UNIT = GameResult.ok(Unit.INSTANCE);

	private final T ok;
	private final ITextComponent error;

	private GameResult(T ok, ITextComponent error) {
		this.ok = ok;
		this.error = error;
	}

	public static <T> GameResult<T> ok(T ok) {
		return new GameResult<>(ok, null);
	}

	public static GameResult<Unit> ok() {
		return OK_UNIT;
	}

	public static <T> GameResult<T> error(ITextComponent error) {
		return new GameResult<>(null, error);
	}

	public static <T> GameResult<T> error(GameException exception) {
		return new GameResult<>(null, exception.getTextMessage());
	}

	public static <T> GameResult<T> fromException(String message, Exception exception) {
		exception.printStackTrace();
		return GameResult.error(new StringTextComponent(message + ": " + exception.toString()));
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

	public <U> GameResult<U> map(Function<T, U> function) {
		if (ok != null) {
			return GameResult.ok(function.apply(ok));
		} else {
			return castError();
		}
	}

	public <U> GameResult<U> flatMap(Function<T, GameResult<U>> function) {
		if (ok != null) {
			return function.apply(ok);
		} else {
			return castError();
		}
	}

	public <U> GameResult<U> mapValue(U value) {
		if (ok != null) {
			return GameResult.ok(value);
		} else {
			return castError();
		}
	}

	public T orElseGet(Function<ITextComponent, T> orElse) {
		if (ok != null) {
			return ok;
		} else {
			return orElse.apply(error);
		}
	}

	@SuppressWarnings("unchecked")
	public <U> GameResult<U> castError() {
		if (error != null) {
			return (GameResult<U>) this;
		} else {
			throw new UnsupportedOperationException("not an error!");
		}
	}
}
