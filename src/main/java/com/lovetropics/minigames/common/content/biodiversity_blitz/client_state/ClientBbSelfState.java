package com.lovetropics.minigames.common.content.biodiversity_blitz.client_state;

import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ClientBbSelfState(int currency, int nextIncrement) implements GameClientState {
	public static final MapCodec<ClientBbSelfState> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.INT.fieldOf("currency").forGetter(c -> c.currency),
			Codec.INT.fieldOf("next_increment").forGetter(c -> c.nextIncrement)
	).apply(i, ClientBbSelfState::new));

	@Override
	public GameClientStateType<?> getType() {
		return BiodiversityBlitz.SELF_STATE.get();
	}
}
