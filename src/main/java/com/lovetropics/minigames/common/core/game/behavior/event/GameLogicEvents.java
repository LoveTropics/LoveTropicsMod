package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.state.GamePhase;
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

	public static final GameEventType<PhaseChange> PHASE_CHANGE = GameEventType.create(PhaseChange.class, listeners -> (phase, lastPhase) -> {
		for (PhaseChange listener : listeners) {
			listener.onPhaseChange(phase, lastPhase);
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

	public interface PhaseChange {
		void onPhaseChange(GamePhase phase, GamePhase lastPhase);
	}
}
