package com.lovetropics.minigames.common.core.game.client_state.instance;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;

public class DisableBobbingState implements GameClientState {
	public static final DisableBobbingState INSTANCE = new DisableBobbingState();

	private DisableBobbingState() {
	}

	@Override
	public GameClientStateType<?> getType() {
		return GameClientStateTypes.DISABLE_BOBBING.get();
	}
}
