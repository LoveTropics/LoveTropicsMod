package com.lovetropics.minigames.common.core.game.client_state.instance;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ResourceLocation;

public final class HotbarTextureClientState implements GameClientState {
	public static final Codec<HotbarTextureClientState> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				ResourceLocation.CODEC.fieldOf("texture").forGetter(c -> c.texture)
		).apply(instance, HotbarTextureClientState::new);
	});

	private final ResourceLocation texture;

	public HotbarTextureClientState(ResourceLocation texture) {
		this.texture = texture;
	}

	public ResourceLocation getTexture() {
		return texture;
	}

	@Override
	public GameClientStateType<?> getType() {
		return GameClientStateTypes.HOTBAR_TEXTURE.get();
	}
}
