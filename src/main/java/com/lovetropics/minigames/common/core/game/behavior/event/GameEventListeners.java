package com.lovetropics.minigames.common.core.game.behavior.event;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public final class GameEventListeners {
	private final Reference2ObjectMap<GameEventType<?>, List<Object>> listeners = new Reference2ObjectOpenHashMap<>();
	private final Reference2ObjectMap<GameEventType<?>, Object> invokers = new Reference2ObjectOpenHashMap<>();

	public <T> void listen(GameEventType<T> event, T listener) {
		List<Object> listeners = this.listeners.computeIfAbsent(event, e -> new ArrayList<>());
		listeners.add(listener);
		this.rebuildInvoker(event);
	}

	private <T> void rebuildInvoker(GameEventType<T> event) {
		Object combined = event.combineUnchecked(this.listeners.get(event));
		this.invokers.put(event, combined);
	}

	@SuppressWarnings("unchecked")
	@Nonnull
	public <T> T invoker(GameEventType<T> event) {
		return (T) this.invokers.computeIfAbsent(event, GameEventType::createEmpty);
	}
}
