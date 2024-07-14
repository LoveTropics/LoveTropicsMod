package com.lovetropics.minigames.common.core.game.client_state;

import com.mojang.serialization.MapCodec;

public record GameClientStateType<T extends GameClientState>(MapCodec<T> codec) {
}
