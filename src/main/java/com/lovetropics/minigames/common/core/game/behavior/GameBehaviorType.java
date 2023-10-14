package com.lovetropics.minigames.common.core.game.behavior;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public record GameBehaviorType<T extends IGameBehavior>(Codec<T> codec) {
	@Override
	public String toString() {
		ResourceLocation key = GameBehaviorTypes.REGISTRY.get().getKey(this);
		return key != null ? key.toString() : "[unregistered]";
	}
}
