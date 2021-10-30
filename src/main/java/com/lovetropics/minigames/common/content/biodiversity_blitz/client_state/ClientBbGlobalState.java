package com.lovetropics.minigames.common.content.biodiversity_blitz.client_state;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;
import java.util.UUID;

// TODO: consolidate all state types by using PartialUpdate system
public final class ClientBbGlobalState implements GameClientState {
	public static final Codec<ClientBbGlobalState> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.unboundedMap(MoreCodecs.UUID_STRING, Codec.INT).fieldOf("currency").forGetter(c -> c.currency)
		).apply(instance, ClientBbGlobalState::new);
	});

	private final Map<UUID, Integer> currency;

	public ClientBbGlobalState(Map<UUID, Integer> currency) {
		this.currency = currency;
	}

	@Override
	public GameClientStateType<?> getType() {
		return BiodiversityBlitz.GLOBAL_STATE.get();
	}

	public boolean hasCurrencyFor(UUID id) {
		return currency.containsKey(id);
	}

	public int getCurrencyFor(UUID id) {
		return currency.getOrDefault(id, 0);
	}
}
