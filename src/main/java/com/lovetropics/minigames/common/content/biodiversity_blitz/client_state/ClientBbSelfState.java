package com.lovetropics.minigames.common.content.biodiversity_blitz.client_state;

import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class ClientBbSelfState implements GameClientState {
	public static final Codec<ClientBbSelfState> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.fieldOf("currency").forGetter(c -> c.currency),
				Codec.INT.fieldOf("next_increment").forGetter(c -> c.nextIncrement)
		).apply(instance, ClientBbSelfState::new);
	});

	private final int currency;
	private final int nextIncrement;

	public ClientBbSelfState(int currency, int nextIncrement) {
		this.currency = currency;
		this.nextIncrement = nextIncrement;
	}

	@Override
	public GameClientStateType<?> getType() {
		return BiodiversityBlitz.SELF_STATE.get();
	}

	public int getCurrency() {
		return currency;
	}

	public int getNextIncrement() {
		return nextIncrement;
	}
}
