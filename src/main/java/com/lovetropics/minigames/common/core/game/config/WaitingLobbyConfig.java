package com.lovetropics.minigames.common.core.game.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;

public final class WaitingLobbyConfig {
	public static final Codec<WaitingLobbyConfig> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				ResourceLocation.CODEC.fieldOf("map").forGetter(c -> c.map)
		).apply(instance, WaitingLobbyConfig::new);
	});

	private final ResourceLocation map;

	public WaitingLobbyConfig(ResourceLocation map) {
		this.map = map;
	}

	public ResourceLocation map() {
		return this.map;
	}
}
