package com.lovetropics.minigames.common.core.game.state;

import java.util.function.Supplier;

public class GameStateType<T extends IGameState> {
	private final String name;

	private GameStateType(String name) {
		this.name = name;
	}

	public static <T extends IGameState> GameStateType<T> create(String name) {
		return new GameStateType<>(name);
	}

	public static <T extends IGameState> GameStateType.Defaulted<T> create(String name, Supplier<T> defaultFactory) {
		return new GameStateType.Defaulted<>(name, defaultFactory);
	}

	public String getName() {
		return name;
	}

	public static final class Defaulted<T extends IGameState> extends GameStateType<T> {
		private final Supplier<T> defaultFactory;

		Defaulted(String name, Supplier<T> defaultFactory) {
			super(name);
			this.defaultFactory = defaultFactory;
		}

		public T createDefault() {
			return this.defaultFactory.get();
		}
	}
}
