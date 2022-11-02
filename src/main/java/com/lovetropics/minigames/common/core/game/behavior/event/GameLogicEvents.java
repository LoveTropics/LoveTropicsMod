package com.lovetropics.minigames.common.core.game.behavior.event;

import net.minecraft.network.chat.Component;

public final class GameLogicEvents {
	public static final GameEventType<WinTriggered> WIN_TRIGGERED = GameEventType.create(WinTriggered.class, listeners -> (winnerName) -> {
		for (WinTriggered listener : listeners) {
			listener.onWinTriggered(winnerName);
		}
	});

	public static final GameEventType<GameOver> GAME_OVER = GameEventType.create(GameOver.class, listeners -> () -> {
		for (GameOver listener : listeners) {
			listener.onGameOver();
		}
	});

	private GameLogicEvents() {
	}

	public interface WinTriggered {
		void onWinTriggered(Component winnerName);
	}

	public interface GameOver {
		void onGameOver();
	}
}
