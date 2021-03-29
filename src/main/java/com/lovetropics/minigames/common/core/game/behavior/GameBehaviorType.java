package com.lovetropics.minigames.common.core.game.behavior;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class GameBehaviorType<T> extends ForgeRegistryEntry<GameBehaviorType<?>> {
	public final Codec<T> codec;

	public GameBehaviorType(Codec<T> codec) {
		this.codec = codec;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Class<GameBehaviorType<?>> wildcardType() {
		return (Class<GameBehaviorType<?>>) (Class) GameBehaviorType.class;
	}
}
