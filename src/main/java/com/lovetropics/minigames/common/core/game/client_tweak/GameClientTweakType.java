package com.lovetropics.minigames.common.core.game.client_tweak;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class GameClientTweakType<T extends GameClientTweak> extends ForgeRegistryEntry<GameClientTweakType<?>> {
	private final Codec<T> codec;

	public GameClientTweakType(Codec<T> codec) {
		this.codec = codec;
	}

	public Codec<T> getCodec() {
		return codec;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Class<GameClientTweakType<?>> type() {
		return (Class<GameClientTweakType<?>>) (Class) GameClientTweakType.class;
	}
}
