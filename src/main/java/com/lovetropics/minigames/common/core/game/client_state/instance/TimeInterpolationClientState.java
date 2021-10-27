package com.lovetropics.minigames.common.core.game.client_state.instance;

import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateType;
import com.lovetropics.minigames.common.core.game.client_state.GameClientStateTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class TimeInterpolationClientState implements GameClientState {
	public static final Codec<TimeInterpolationClientState> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.fieldOf("speed").forGetter(c -> c.speed)
		).apply(instance, TimeInterpolationClientState::new);
	});

	private final int speed;

	public TimeInterpolationClientState(int speed) {
		this.speed = speed;
	}

	public int getSpeed() {
		return speed;
	}

	@Override
	public GameClientStateType<?> getType() {
		return GameClientStateTypes.TIME_INTERPOLATION.get();
	}
}
