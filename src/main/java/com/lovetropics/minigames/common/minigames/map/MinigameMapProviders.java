package com.lovetropics.minigames.common.minigames.map;

import com.lovetropics.minigames.common.CodecRegistry;
import com.lovetropics.minigames.common.Util;
import com.mojang.serialization.Codec;
import net.minecraft.util.ResourceLocation;

import java.util.function.Function;

public final class MinigameMapProviders {
	public static final CodecRegistry<ResourceLocation, Codec<? extends IMinigameMapProvider>> REGISTRY = CodecRegistry.resourceLocationKeys();

	public static final Codec<IMinigameMapProvider> CODEC = REGISTRY.dispatchStable(IMinigameMapProvider::getCodec, Function.identity());

	private static void register(final String name, final Codec<? extends IMinigameMapProvider> codec) {
		REGISTRY.register(Util.resource(name), codec);
	}

	static {
		register("load_map", LoadMapProvider.CODEC);
		register("random", RandomMapProvider.CODEC);
		register("inline", InlineMapProvider.CODEC);
	}
}
