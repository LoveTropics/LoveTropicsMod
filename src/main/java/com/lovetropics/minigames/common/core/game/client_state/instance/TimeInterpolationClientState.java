package com.lovetropics.minigames.common.core.game.client_state.instance;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record TimeInterpolationClientState(int speed) implements GameClientState {
	public static final Codec<TimeInterpolationClientState> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.fieldOf("speed").forGetter(c -> c.speed)
	).apply(i, TimeInterpolationClientState::new));

	@Override
	public GameClientStateType<?> getType() {
		return GameClientStateTypes.TIME_INTERPOLATION.get();
	}
}
