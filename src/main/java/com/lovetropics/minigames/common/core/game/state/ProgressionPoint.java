package com.lovetropics.minigames.common.core.game.state;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.SharedConstants;

import javax.annotation.Nullable;
import java.util.function.BooleanSupplier;

public interface ProgressionPoint {
	Codec<ProgressionPoint> CODEC = createCodec(Direct.CODEC, Named.CODEC);
	Codec<ProgressionPoint> STRING_CODEC = createCodec(Direct.STRING_CODEC, Named.CODEC);

	private static Codec<ProgressionPoint> createCodec(final Codec<Direct> directCodec, final Codec<Named> namedCodec) {
		return new Codec<>() {
			@Override
			public <T> DataResult<Pair<ProgressionPoint, T>> decode(DynamicOps<T> ops, T input) {
				DataResult<Pair<Direct, T>> direct = directCodec.decode(ops, input);
				if (direct.error().isPresent()) {
					return namedCodec.decode(ops, input).map(r -> r.mapFirst(p -> p));
				}
				return direct.map(r -> r.mapFirst(p -> p));
			}

			@Override
			public <T> DataResult<T> encode(ProgressionPoint input, DynamicOps<T> ops, T prefix) {
				if (input instanceof final Named named) {
					return namedCodec.encode(named, ops, prefix);
				} else if (input instanceof final Direct direct) {
					return directCodec.encode(direct, ops, prefix);
				}
				throw new UnsupportedOperationException();
			}
		};
	}

	int UNRESOLVED = -1;

	int resolve(@Nullable GameProgressionState progression);

	default BooleanSupplier createPredicate(final IGamePhase game) {
		GameProgressionState progression = game.state().getOrNull(GameProgressionState.KEY);
		if (progression != null) {
			return () -> progression.isAfter(this);
		} else {
			final int time = resolve(null);
			return () -> game.ticks() >= time;
		}
	}

	record Direct(float value) implements ProgressionPoint {
		public static final Codec<Direct> CODEC = Codec.FLOAT.xmap(Direct::new, Direct::value);
		public static final Codec<Direct> STRING_CODEC = Codec.STRING.comapFlatMap(string -> {
			try {
				return DataResult.success(new Direct(Float.parseFloat(string)));
			} catch (final NumberFormatException e) {
				return DataResult.error(() -> "Not a number: " + string);
			}
		}, direct -> String.valueOf(direct.value()));

		@Override
		public int resolve(@Nullable final GameProgressionState progression) {
			return Math.round(value * SharedConstants.TICKS_PER_SECOND);
		}
	}

	record Named(String name) implements ProgressionPoint {
		public static final Codec<Named> CODEC = Codec.STRING.xmap(Named::new, Named::name);

		@Override
		public int resolve(@Nullable final GameProgressionState progression) {
			return progression != null ? progression.getNamedPoint(name) : UNRESOLVED;
		}
	}
}
