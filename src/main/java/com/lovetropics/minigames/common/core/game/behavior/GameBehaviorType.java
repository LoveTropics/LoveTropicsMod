package com.lovetropics.minigames.common.core.game.behavior;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class GameBehaviorType<T extends IGameBehavior> extends ForgeRegistryEntry<GameBehaviorType<?>> {
	public final Codec<T> codec;

	public GameBehaviorType(Codec<T> codec) {
		this.codec = codec;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Class<GameBehaviorType<?>> type() {
		return (Class<GameBehaviorType<?>>) (Class) GameBehaviorType.class;
	}
}
