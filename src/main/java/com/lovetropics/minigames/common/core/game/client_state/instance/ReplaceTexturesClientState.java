package com.lovetropics.minigames.common.core.game.client_state.instance;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Map;

public final class ReplaceTexturesClientState implements GameClientState {
	public static final Codec<ReplaceTexturesClientState> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.unboundedMap(TextureType.CODEC, ResourceLocation.CODEC).fieldOf("textures").forGetter(c -> c.textures)
		).apply(instance, ReplaceTexturesClientState::new);
	});

	private final Map<TextureType, ResourceLocation> textures;

	public ReplaceTexturesClientState(Map<TextureType, ResourceLocation> textures) {
		this.textures = textures;
	}

	@Nullable
	public ResourceLocation getTexture(TextureType type) {
		return textures.get(type);
	}

	@Override
	public GameClientStateType<?> getType() {
		return GameClientStateTypes.REPLACE_TEXTURES.get();
	}

	public enum TextureType implements IStringSerializable {
		HOTBAR("hotbar"),
		BOSS_BARS("boss_bars");

		public static final Codec<TextureType> CODEC = MoreCodecs.stringVariants(values(), TextureType::getString);

		private final String key;

		TextureType(String key) {
			this.key = key;
		}

		@Override
		public String getString() {
			return key;
		}
	}
}
