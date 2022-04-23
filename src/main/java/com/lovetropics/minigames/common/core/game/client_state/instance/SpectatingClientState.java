package com.lovetropics.minigames.common.core.game.client_state.instance;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.SerializableUUID;

import java.util.List;
import java.util.UUID;

public final class SpectatingClientState implements GameClientState {
	public static final Codec<SpectatingClientState> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				SerializableUUID.CODEC.listOf().fieldOf("players").forGetter(c -> c.players)
		).apply(instance, SpectatingClientState::new);
	});

	private final List<UUID> players;

	public SpectatingClientState(List<UUID> players) {
		this.players = players;
	}

	public List<UUID> getPlayers() {
		return players;
	}

	@Override
	public GameClientStateType<?> getType() {
		return GameClientStateTypes.SPECTATING.get();
	}
}
