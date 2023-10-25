package com.lovetropics.minigames.common.content.biodiversity_blitz.client_state;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ClientBbMobSpawnState(BlockBox plot) implements GameClientState {
    public static final Codec<ClientBbMobSpawnState> CODEC = RecordCodecBuilder.create(i -> i.group(
            BlockBox.CODEC.fieldOf("plots").forGetter(c -> c.plot)
    ).apply(i, ClientBbMobSpawnState::new));

    @Override
    public GameClientStateType<?> getType() {
        return BiodiversityBlitz.MOB_SPAWN.get();
    }
}
