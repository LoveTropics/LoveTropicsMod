package com.lovetropics.minigames.common.core.game.client_state.instance;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import javax.annotation.Nullable;
import java.util.Map;

public record ReplaceTexturesClientState(Map<TextureType, ResourceLocation> textures) implements GameClientState {
	public static final MapCodec<ReplaceTexturesClientState> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.unboundedMap(TextureType.CODEC, ResourceLocation.CODEC).fieldOf("textures").forGetter(c -> c.textures)
	).apply(i, ReplaceTexturesClientState::new));

	@Nullable
	public ResourceLocation getTexture(TextureType type) {
		return textures.get(type);
	}

	@Override
	public GameClientStateType<?> getType() {
		return GameClientStateTypes.REPLACE_TEXTURES.get();
	}

	public enum TextureType implements StringRepresentable {
		HOTBAR("hotbar"),
		;

		public static final Codec<TextureType> CODEC = MoreCodecs.stringVariants(values(), TextureType::getSerializedName);

		private final String key;

		TextureType(String key) {
			this.key = key;
		}

		@Override
		public String getSerializedName() {
			return key;
		}
	}
}
