package com.lovetropics.minigames.common.core.game.state;

import com.lovetropics.minigames.common.core.game.GameException;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public final class GameStateMap {
	private final Map<GameStateType<?>, IGameState> state = new Reference2ObjectOpenHashMap<>();

	public <T extends IGameState> T register(GameStateType<T> type, T state) {
		if (this.state.putIfAbsent(type, state) == null) {
			return state;
		} else {
			throw new IllegalStateException("Multiple callers tried to register game state of type: " + type.getName());
		}
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public <T extends IGameState> T get(GameStateType<T> type) {
		return (T) this.state.get(type);
	}

	public <T extends IGameState> Optional<T> getOptional(GameStateType<T> type) {
		return Optional.ofNullable(this.get(type));
	}

	public <T extends IGameState> T getOrThrow(GameStateType<T> type) {
		T state = this.get(type);
		if (state == null) {
			throw new GameException(new StringTextComponent("Missing expected game state of type: " + type.getName()));
		}
		return state;
	}
}
