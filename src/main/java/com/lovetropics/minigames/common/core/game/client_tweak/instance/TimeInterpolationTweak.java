package com.lovetropics.minigames.common.core.game.client_tweak.instance;

import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweak;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakType;
import com.lovetropics.minigames.common.core.game.client_tweak.GameClientTweakTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class TimeInterpolationTweak implements GameClientTweak {
	public static final Codec<TimeInterpolationTweak> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.fieldOf("speed").forGetter(c -> c.speed)
		).apply(instance, TimeInterpolationTweak::new);
	});

	private final int speed;

	public TimeInterpolationTweak(int speed) {
		this.speed = speed;
	}

	public int getSpeed() {
		return speed;
	}

	@Override
	public GameClientTweakType<?> getType() {
		return GameClientTweakTypes.TIME_INTERPOLATION.get();
	}
}
