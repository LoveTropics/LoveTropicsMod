package com.lovetropics.minigames.common.core.game.client_state.instance;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class ResourcePackClientState implements GameClientState {
	public static final Codec<ResourcePackClientState> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("pack_name").forGetter(c -> c.packName)
		).apply(instance, ResourcePackClientState::new);
	});

	private final String packName;

	public ResourcePackClientState(String packName) {
		this.packName = packName;
	}

	public String getPackName() {
		return packName;
	}

	@Override
	public GameClientStateType<?> getType() {
		return GameClientStateTypes.RESOURCE_PACK.get();
	}
}
