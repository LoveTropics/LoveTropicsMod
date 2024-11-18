package com.lovetropics.minigames.common.core.game.client_state.instance;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;

public class DisablePlayerMovementState implements GameClientState {
	public static final DisablePlayerMovementState INSTANCE = new DisablePlayerMovementState();

	private DisablePlayerMovementState() {
	}

	@Override
	public GameClientStateType<?> getType() {
		return GameClientStateTypes.DISABLE_PLAYER_MOVEMENT.get();
	}
}
