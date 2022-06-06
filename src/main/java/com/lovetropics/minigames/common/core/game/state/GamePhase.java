package com.lovetropics.minigames.common.core.game.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// TODO: confusing duplicate naming
public record GamePhase(String key, int lengthInTicks) {
	public static final Codec<GamePhase> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.fieldOf("key").forGetter(c -> c.key),
			Codec.INT.fieldOf("length_in_ticks").forGetter(c -> c.lengthInTicks)
	).apply(i, GamePhase::new));

	public boolean is(String phase) {
		return this.key.equals(phase);
	}
}
