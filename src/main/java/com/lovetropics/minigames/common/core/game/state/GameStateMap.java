package com.lovetropics.minigames.common.core.game.state;

import com.lovetropics.minigames.common.core.game.GameException;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.network.chat.TextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public final class GameStateMap {
	private final Map<GameStateKey<?>, IGameState> state = new Reference2ObjectOpenHashMap<>();

	public <T extends IGameState> T register(GameStateKey<T> key, T state) {
		if (this.state.putIfAbsent(key, state) == null) {
			return state;
		} else {
			throw new IllegalStateException("Multiple callers tried to register game state of key: " + key.getName());
		}
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public <T extends IGameState> T get(GameStateKey.Defaulted<T> key) {
		return (T) this.state.computeIfAbsent(key, k -> key.createDefault());
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends IGameState> T getOrNull(GameStateKey<T> key) {
		return (T) this.state.get(key);
	}

	public <T extends IGameState> Optional<T> getOptional(GameStateKey<T> key) {
		return Optional.ofNullable(this.getOrNull(key));
	}

	public <T extends IGameState> T getOrThrow(GameStateKey<T> key) {
		T state = this.getOrNull(key);
		if (state == null) {
			throw new GameException(new TextComponent("Missing expected game state of key: " + key.getName()));
		}
		return state;
	}
}
