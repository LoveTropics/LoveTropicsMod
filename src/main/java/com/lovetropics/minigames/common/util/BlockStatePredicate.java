package com.lovetropics.minigames.common.util;

import com.lovetropics.lib.codec.MoreCodecs;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.tags.Tag;
import net.minecraft.tags.SerializationTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;

import java.util.function.Function;
import java.util.function.Predicate;

public interface BlockStatePredicate extends Predicate<BlockState> {
	Codec<BlockStatePredicate> CODEC = new Codec<BlockStatePredicate>() {
		@Override
		public <T> DataResult<Pair<BlockStatePredicate, T>> decode(DynamicOps<T> ops, T input) {
			DataResult<Pair<AnyOf, T>> any = AnyOf.CODEC.decode(ops, input);
			if (any.result().isPresent()) {
				return any.map(p -> p.mapFirst(Function.identity()));
			}

			DataResult<Pair<MatchTag, T>> tag = MatchTag.CODEC.decode(ops, input);
			if (tag.result().isPresent()) {
				return tag.map(p -> p.mapFirst(Function.identity()));
			}

			return MatchBlock.CODEC.decode(ops, input).map(p -> p.mapFirst(Function.identity()));
		}

		@Override
		public <T> DataResult<T> encode(BlockStatePredicate predicate, DynamicOps<T> ops, T prefix) {
			return encodeUnchecked(predicate.getCodec(), predicate, ops, prefix);
		}
	};

	BlockStatePredicate ANY = new Any();

	@Override
	boolean test(BlockState state);

	Codec<? extends BlockStatePredicate> getCodec();

	@SuppressWarnings("unchecked")
	static <A, T> DataResult<T> encodeUnchecked(Codec<A> codec, Object input, DynamicOps<T> ops, T prefix) {
		return codec.encode((A) input, ops, prefix);
	}

	final class MatchBlock implements BlockStatePredicate {
		public static final Codec<MatchBlock> CODEC = Registry.BLOCK.xmap(MatchBlock::new, c -> c.block);

		private final Block block;

		public MatchBlock(Block block) {
			this.block = block;
		}

		@Override
		public boolean test(BlockState state) {
			return state.getBlock().is(this.block);
		}

		@Override
		public Codec<? extends BlockStatePredicate> getCodec() {
			return CODEC;
		}
	}

	final class MatchTag implements BlockStatePredicate {
		public static final Codec<MatchTag> CODEC = Codec.STRING.comapFlatMap(
				string -> {
					if (string.startsWith("#")) {
						ResourceLocation id = new ResourceLocation(string.substring(1));
						Tag<Block> tag = SerializationTags.getInstance().getBlocks().getTag(id);
						return tag != null ? DataResult.success(new MatchTag(tag)) : DataResult.error("no tag exists with id: '" + id + "'");
					} else {
						return DataResult.error("not in tag format: must start with #");
					}
				},
				c -> "#" + SerializationTags.getInstance().getBlocks().getIdOrThrow(c.tag)
		);

		private final Tag<Block> tag;

		public MatchTag(Tag<Block> tag) {
			this.tag = tag;
		}

		@Override
		public boolean test(BlockState state) {
			return state.is(this.tag);
		}

		@Override
		public Codec<? extends BlockStatePredicate> getCodec() {
			return CODEC;
		}
	}

	final class AnyOf implements BlockStatePredicate {
		public static final Codec<AnyOf> CODEC = MoreCodecs.listToArray(BlockStatePredicate.CODEC.listOf(), BlockStatePredicate[]::new)
				.xmap(AnyOf::new, c -> c.predicates);

		private final BlockStatePredicate[] predicates;

		public AnyOf(BlockStatePredicate[] predicates) {
			this.predicates = predicates;
		}

		@Override
		public boolean test(BlockState state) {
			for (BlockStatePredicate predicate : this.predicates) {
				if (predicate.test(state)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Codec<? extends BlockStatePredicate> getCodec() {
			return CODEC;
		}
	}

	final class Any implements BlockStatePredicate {
		public static final Codec<Any> CODEC = Codec.unit(Any::new);

		@Override
		public boolean test(BlockState state) {
			return true;
		}

		@Override
		public Codec<? extends BlockStatePredicate> getCodec() {
			return CODEC;
		}
	}
}
