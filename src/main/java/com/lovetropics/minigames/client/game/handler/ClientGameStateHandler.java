package com.lovetropics.minigames.client.game.handler;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;

public interface ClientGameStateHandler<T extends GameClientState> {
	void accept(T state);

	void disable(T state);
}
