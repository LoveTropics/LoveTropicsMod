package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.GameWinner;
import com.lovetropics.minigames.common.core.game.state.team.GameTeam;
import net.minecraft.server.level.ServerPlayer;

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
		void onWinTriggered(GameWinner winner);

		default void onWinTriggered(ServerPlayer player) {
			onWinTriggered(new GameWinner.Player(player));
		}

		default void onWinTriggered(GameTeam team) {
			onWinTriggered(new GameWinner.Team(team));
		}

		default void onStalemate() {
			onWinTriggered(new GameWinner.Nobody());
		}
	}

	public interface GameOver {
		void onGameOver();
	}
}
