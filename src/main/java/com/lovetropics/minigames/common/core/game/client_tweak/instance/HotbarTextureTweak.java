package com.lovetropics.minigames.common.core.game.client_tweak.instance;

import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweak;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakType;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;

public final class HotbarTextureTweak implements GameClientTweak {
	public static final Codec<HotbarTextureTweak> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				ResourceLocation.CODEC.fieldOf("texture").forGetter(c -> c.texture)
		).apply(instance, HotbarTextureTweak::new);
	});

	private final ResourceLocation texture;

	public HotbarTextureTweak(ResourceLocation texture) {
		this.texture = texture;
	}

	public ResourceLocation getTexture() {
		return texture;
	}

	@Override
	public GameClientTweakType<?> getType() {
		return GameClientTweakTypes.HOTBAR_TEXTURE.get();
	}
}
