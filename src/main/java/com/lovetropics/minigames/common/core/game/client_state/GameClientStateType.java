package com.lovetropics.minigames.common.core.game.client_state;

import com.mojang.serialization.Codec;

public record GameClientStateType<T extends GameClientState>(Codec<T> codec) {
}
