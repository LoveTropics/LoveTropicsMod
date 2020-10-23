package com.lovetropics.minigames.common.minigames.behaviours;

import com.lovetropics.minigames.common.minigames.MinigameResult;
import net.minecraft.util.Unit;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface BehaviorDispatcher<T, S extends BehaviorDispatcher<T, S>> {
	Collection<T> getBehaviors();

	default <A> MinigameResult<Unit> dispatchToBehaviors(TriConsumer<T, S, A> action, A argument) {
		return dispatchToBehaviors((b, m) -> action.accept(b, m, argument));
	}

	@SuppressWarnings("unchecked")
	default MinigameResult<Unit> dispatchToBehaviors(BiConsumer<T, S> action) {
		try {
			for (T behavior : getBehaviors()) {
				action.accept(behavior, (S) this);
			}
			return MinigameResult.ok();
		} catch (Exception e) {
			return MinigameResult.fromException("Failed to dispatch to behaviors", e);
		}
	}

	default MinigameResult<Unit> dispatchToBehaviors(Function<T, MinigameResult<Unit>> action) {
		try {
			for (T behavior : getBehaviors()) {
				MinigameResult<Unit> result = action.apply(behavior);
				if (result.isError()) {
					return result;
				}
			}
			return MinigameResult.ok();
		} catch (Exception e) {
			return MinigameResult.fromException("Failed to dispatch to behaviors", e);
		}
	}
}
