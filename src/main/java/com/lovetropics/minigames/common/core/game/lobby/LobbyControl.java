package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.GameResult;
import net.minecraft.util.Unit;

public interface LobbyControl {
	GameResult<Unit> run();

	enum Type {
		PLAY,
		PAUSE,
		STOP
	}
}
