package com.lovetropics.minigames.common.core.game.state;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.SharedConstants;

public interface ProgressionPoint {
	Codec<ProgressionPoint> CODEC = new Codec<>() {
		@Override
		public <T> DataResult<Pair<ProgressionPoint, T>> decode(DynamicOps<T> ops, T input) {
			DataResult<Pair<Direct, T>> direct = Direct.CODEC.decode(ops, input);
			if (direct.error().isPresent()) {
				return Named.CODEC.decode(ops, input).map(r -> r.mapFirst(p -> p));
			}
			return direct.map(r -> r.mapFirst(p -> p));
		}

		@Override
		public <T> DataResult<T> encode(ProgressionPoint input, DynamicOps<T> ops, T prefix) {
			return encodeUnchecked(input, ops, prefix);
		}

		@SuppressWarnings("unchecked")
		private static <T, P extends ProgressionPoint> DataResult<T> encodeUnchecked(P input, DynamicOps<T> ops, T prefix) {
			Codec<P> codec = (Codec<P>) input.codec();
			return codec.encode(input, ops, prefix);
		}
	};

	int UNRESOLVED = -1;

	int resolve(GameProgressionState progression);

	Codec<? extends ProgressionPoint> codec();

	record Direct(float value) implements ProgressionPoint {
		public static final Codec<Direct> CODEC = Codec.FLOAT.xmap(Direct::new, Direct::value);

		@Override
		public int resolve(GameProgressionState progression) {
			return Math.round(value * SharedConstants.TICKS_PER_SECOND);
		}

		@Override
		public Codec<? extends ProgressionPoint> codec() {
			return CODEC;
		}
	}

	record Named(String name) implements ProgressionPoint {
		public static final Codec<Named> CODEC = Codec.STRING.xmap(Named::new, Named::name);

		@Override
		public int resolve(GameProgressionState progression) {
			return progression.getNamedPoint(name);
		}

		@Override
		public Codec<? extends ProgressionPoint> codec() {
			return CODEC;
		}
	}
}
