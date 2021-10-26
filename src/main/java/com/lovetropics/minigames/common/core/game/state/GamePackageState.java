package com.lovetropics.minigames.common.core.game.state;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Set;
import java.util.stream.Stream;

public final class GamePackageState implements IGameState {
	public static final GameStateKey.Defaulted<GamePackageState> KEY = GameStateKey.create("Game Packages", GamePackageState::new);

	private final Set<String> knownPackages = new ObjectOpenHashSet<>();

	public void addPackageType(String type) {
		this.knownPackages.add(type);
	}

	public Stream<String> stream() {
		return this.knownPackages.stream();
	}
}
