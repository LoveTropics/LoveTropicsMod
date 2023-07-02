package com.lovetropics.minigames.common.core.game.behavior;

import com.mojang.serialization.Codec;

public record GameBehaviorType<T extends IGameBehavior>(Codec<T> codec) {
	@Override
	public String toString() {
		return GameBehaviorTypes.REGISTRY.get().getKey(this).toString();
	}
}
