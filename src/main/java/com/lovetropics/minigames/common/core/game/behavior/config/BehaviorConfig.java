package com.lovetropics.minigames.common.core.game.behavior.config;

import com.mojang.serialization.Codec;

public class BehaviorConfig<T> {

	private final String name;
	private final Codec<T> codec;

	public BehaviorConfig(String name, Codec<T> codec) {
		this.name = name;
		this.codec = codec;
	}
	
	public String getName() {
		return name;
	}
	
	public Codec<T> getCodec() {
		return codec;
	}
}
