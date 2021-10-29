package com.lovetropics.minigames.common.content.biodiversity_blitz.client_state;

import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class CurrencyTargetState implements GameClientState {
	public static final Codec<CurrencyTargetState> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.fieldOf("value").forGetter(c -> c.value)
		).apply(instance, CurrencyTargetState::new);
	});

	private final int value;

	public CurrencyTargetState(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	@Override
	public GameClientStateType<?> getType() {
		return BiodiversityBlitz.CURRENCY_TARGET.get();
	}
}
