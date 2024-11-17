package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.IGamePhase;

public final class SubGameEvents {
	public static final GameEventType<Create> CREATE = GameEventType.create(Create.class, listeners -> (subGame, subEvents) -> {
		for (Create listener : listeners) {
			listener.onCreateSubGame(subGame, subEvents);
		}
	});

	public static final GameEventType<ReturnToTop> RETURN_TO_TOP = GameEventType.create(ReturnToTop.class, listeners -> () -> {
		for (ReturnToTop listener : listeners) {
			listener.onReturnToTopGame();
		}
	});

	private SubGameEvents() {
	}

	public interface Create {
		void onCreateSubGame(IGamePhase subGame, EventRegistrar subEvents);
	}

	public interface ReturnToTop {
		void onReturnToTopGame();
	}
}
