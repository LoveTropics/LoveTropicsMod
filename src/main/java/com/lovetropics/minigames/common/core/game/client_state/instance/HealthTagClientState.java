package com.lovetropics.minigames.common.core.game.client_state.instance;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.Codec;

public record HealthTagClientState() implements GameClientState {
	public static final Codec<HealthTagClientState> CODEC = Codec.unit(HealthTagClientState::new);

	@Override
	public GameClientStateType<?> getType() {
		return GameClientStateTypes.HEALTH_TAG.get();
	}
}
