package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.state.instances.GamePhase;
import net.minecraft.util.text.ITextComponent;

public final class GameLogicEvents {
	public static final GameEventType<WinTriggered> WIN_TRIGGERED = GameEventType.create(WinTriggered.class, listeners -> (game, winnerName) -> {
		for (WinTriggered listener : listeners) {
			listener.onWinTriggered(game, winnerName);
		}
	});

	public static final GameEventType<GameOver> GAME_OVER = GameEventType.create(GameOver.class, listeners -> game -> {
		for (GameOver listener : listeners) {
			listener.onGameOver(game);
		}
	});

	public static final GameEventType<PhaseChange> PHASE_CHANGE = GameEventType.create(PhaseChange.class, listeners -> (game, phase, lastPhase) -> {
		for (PhaseChange listener : listeners) {
			listener.onPhaseChange(game, phase, lastPhase);
		}
	});

	private GameLogicEvents() {
	}

	public interface WinTriggered {
		void onWinTriggered(IActiveGame game, ITextComponent winnerName);
	}

	public interface GameOver {
		void onGameOver(IActiveGame game);
	}

	public interface PhaseChange {
		void onPhaseChange(IActiveGame game, GamePhase phase, GamePhase lastPhase);
	}
}
