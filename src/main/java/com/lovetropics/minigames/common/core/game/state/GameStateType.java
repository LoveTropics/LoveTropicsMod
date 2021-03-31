package com.lovetropics.minigames.common.core.game.state;

public final class GameStateType<T extends IGameState> {
	private final String name;

	private GameStateType(String name) {
		this.name = name;
	}

	public static <T extends IGameState> GameStateType<T> create(String name) {
		return new GameStateType<>(name);
	}

	public String getName() {
		return name;
	}
}
