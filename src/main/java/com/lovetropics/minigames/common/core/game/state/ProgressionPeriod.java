package com.lovetropics.minigames.common.core.game.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record ProgressionPeriod(ProgressionPoint start, ProgressionPoint end) {
	public static final Codec<ProgressionPeriod> CODEC = RecordCodecBuilder.create(i -> i.group(
			ProgressionPoint.CODEC.fieldOf("start").forGetter(ProgressionPeriod::start),
			ProgressionPoint.CODEC.fieldOf("end").forGetter(ProgressionPeriod::end)
	).apply(i, ProgressionPeriod::new));
}
