package com.lovetropics.minigames.common.core.game.behavior;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;

public record GameBehaviorType<T extends IGameBehavior>(Codec<T> codec) {
	@Override
	public String toString() {
		ResourceLocation key = GameBehaviorTypes.REGISTRY.get().getKey(this);
		return key != null ? key.toString() : "[unregistered]";
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		GameBehaviorType<?> that = (GameBehaviorType<?>) object;
		return codec == that.codec;
	}

	@Override
	public int hashCode() {
		return Objects.hash(codec);
	}
}
