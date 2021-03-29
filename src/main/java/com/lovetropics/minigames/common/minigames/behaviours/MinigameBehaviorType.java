package com.lovetropics.minigames.common.minigames.behaviours;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class MinigameBehaviorType<T> extends ForgeRegistryEntry<MinigameBehaviorType<?>> {
	public final Codec<T> codec;

	public MinigameBehaviorType(Codec<T> codec) {
		this.codec = codec;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static Class<MinigameBehaviorType<?>> wildcardType() {
		return (Class<MinigameBehaviorType<?>>) (Class) MinigameBehaviorType.class;
	}
}
