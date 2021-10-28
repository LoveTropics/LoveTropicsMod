package com.lovetropics.minigames.common.core.game.client_state;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistryEntry;

public final class GameClientStateType<T extends GameClientState> extends ForgeRegistryEntry<GameClientStateType<?>> {
	private final Codec<T> codec;

	public GameClientStateType(Codec<T> codec) {
		this.codec = codec;
	}

	public Codec<T> getCodec() {
		return codec;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Class<GameClientStateType<?>> type() {
		return (Class<GameClientStateType<?>>) (Class) GameClientStateType.class;
	}
}
