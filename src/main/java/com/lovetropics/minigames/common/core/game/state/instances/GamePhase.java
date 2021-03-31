package com.lovetropics.minigames.common.core.game.state.instances;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class GamePhase {
	public static final Codec<GamePhase> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.fieldOf("key").forGetter(c -> c.key),
				Codec.INT.fieldOf("length_in_ticks").forGetter(c -> c.lengthInTicks)
		).apply(instance, GamePhase::new);
	});

	public final String key;
	public final int lengthInTicks;

	public GamePhase(String key, int lengthInTicks) {
		this.key = key;
		this.lengthInTicks = lengthInTicks;
	}

	public boolean is(String phase) {
		return this.key.equals(phase);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj != null && getClass() == obj.getClass()) {
			GamePhase phase = (GamePhase) obj;
			return key.equals(phase.key);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return key.hashCode();
	}
}
