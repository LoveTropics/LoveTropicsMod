package com.lovetropics.minigames.common.core.game.state;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.BooleanSupplier;

public record ProgressionPeriod(ProgressionPoint start, ProgressionPoint end) {
	public static final MapCodec<ProgressionPeriod> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			ProgressionPoint.CODEC.fieldOf("start").forGetter(ProgressionPeriod::start),
			ProgressionPoint.CODEC.fieldOf("end").forGetter(ProgressionPeriod::end)
	).apply(i, ProgressionPeriod::new));

	public static final Codec<ProgressionPeriod> CODEC = MAP_CODEC.codec();

	public BooleanSupplier createPredicate(final IGamePhase game) {
		final GameProgressionState progression = game.getState().getOrNull(GameProgressionState.KEY);
		if (progression == null) {
			final int start = this.start.resolve(null);
			final int end = this.end.resolve(null);
			return () -> {
				final long ticks = game.ticks();
				return ticks >= start && ticks <= end;
			};
		}
		return () -> progression.is(this);
	}
}
