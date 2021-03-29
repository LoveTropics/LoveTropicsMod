package com.lovetropics.minigames.common.core.game.behavior;

import com.lovetropics.minigames.common.core.game.GameResult;
import net.minecraft.util.Unit;
import org.apache.logging.log4j.util.TriConsumer;

import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface BehaviorDispatcher<T, S extends BehaviorDispatcher<T, S>> {
	Collection<T> getBehaviors();

	default <A> GameResult<Unit> dispatchToBehaviors(TriConsumer<T, S, A> action, A argument) {
		return dispatchToBehaviors((b, m) -> action.accept(b, m, argument));
	}

	@SuppressWarnings("unchecked")
	default GameResult<Unit> dispatchToBehaviors(BiConsumer<T, S> action) {
		try {
			for (T behavior : getBehaviors()) {
				action.accept(behavior, (S) this);
			}
			return GameResult.ok();
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch to behaviors", e);
		}
	}

	default GameResult<Unit> dispatchToBehaviors(Function<T, GameResult<Unit>> action) {
		try {
			for (T behavior : getBehaviors()) {
				GameResult<Unit> result = action.apply(behavior);
				if (result.isError()) {
					return result;
				}
			}
			return GameResult.ok();
		} catch (Exception e) {
			return GameResult.fromException("Failed to dispatch to behaviors", e);
		}
	}
}
