package com.lovetropics.minigames.common.core.game.behavior.event;

import java.util.function.Function;
import java.util.function.Predicate;

public interface EventRegistrar {
	<T> void listen(GameEventType<T> type, T listener);

	<T> void unlisten(GameEventType<T> type, T listener);

	default void addAll(GameEventListeners listeners) {
		listeners.forEach(this::listen);
	}

	default void removeAll(GameEventListeners listeners) {
		listeners.forEach(this::unlisten);
	}

	default EventRegistrar redirect(Predicate<GameEventType<?>> predicate, EventRegistrar redirect) {
		return mapping(type -> predicate.test(type) ? redirect : this);
	}

	static EventRegistrar mapping(Function<GameEventType<?>, EventRegistrar> redirect) {
		return new EventRegistrar() {
			@Override
			public <T> void listen(GameEventType<T> type, T listener) {
				redirect.apply(type).listen(type, listener);
			}

			@Override
			public <T> void unlisten(GameEventType<T> type, T listener) {
				redirect.apply(type).unlisten(type, listener);
			}
		};
	}
}
