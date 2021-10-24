package com.lovetropics.minigames.common.content.biodiversity_blitz.client_tweak;

import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweak;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// TODO: this can possibly be a more generic UI display tweak
public final class ClientBiodiversityBlitzState implements GameClientTweak {
	public static final Codec<ClientBiodiversityBlitzState> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.fieldOf("currency").forGetter(c -> c.currency),
				Codec.INT.fieldOf("next_increment").forGetter(c -> c.nextIncrement)
		).apply(instance, ClientBiodiversityBlitzState::new);
	});

	private final int currency;
	private final int nextIncrement;

	public ClientBiodiversityBlitzState(int currency, int nextIncrement) {
		this.currency = currency;
		this.nextIncrement = nextIncrement;
	}

	@Override
	public GameClientTweakType<?> getType() {
		return BiodiversityBlitz.CLIENT_STATE_TWEAK.get();
	}

	public int getCurrency() {
		return currency;
	}

	public int getNextIncrement() {
		return nextIncrement;
	}
}
