package com.lovetropics.minigames.common.core.game.behavior.event;

import java.lang.reflect.Array;
import java.util.Collection;

public final class GameEventType<T> {
    private final Class<T> type;
    private final Combinator<T> combinator;

    private GameEventType(Class<T> type, Combinator<T> combinator) {
        this.type = type;
        this.combinator = combinator;
    }

    public static <T> GameEventType<T> create(Class<T> type, Combinator<T> combinator) {
        return new GameEventType<>(type, combinator);
    }

    public T combine(T[] listeners) {
        return this.combinator.apply(listeners);
    }

    @SuppressWarnings("unchecked")
    public <U> T combineUnchecked(Collection<U> listeners) {
        T[] array = (T[]) Array.newInstance(this.type, listeners.size());
        return this.combine(listeners.toArray(array));
    }

    @SuppressWarnings("unchecked")
    public T createEmpty() {
        T[] array = (T[]) Array.newInstance(this.type, 0);
        return this.combinator.apply(array);
    }

    public interface Combinator<T> {
        T apply(T[] listeners);
    }
}
