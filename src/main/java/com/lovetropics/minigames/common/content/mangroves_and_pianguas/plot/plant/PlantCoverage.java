package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.mojang.serialization.codecs.PrimitiveCodec;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Iterator;
import java.util.Random;

public interface PlantCoverage extends Iterable<BlockPos> {
	static PlantCoverage of(BlockPos block) {
		return new Single(block);
	}

	static PlantCoverage ofDouble(BlockPos block) {
		LongSet blocks = new LongOpenHashSet(2);
		blocks.add(block.toLong());
		blocks.add(block.up().toLong());
		return new Set(blocks, block);
	}

	static PlantCoverage of(LongSet blocks, BlockPos origin) {
		return new Set(blocks, origin);
	}

	boolean covers(BlockPos pos);

	BlockPos random(Random random);

	AxisAlignedBB asBounds();

	BlockPos getOrigin();

	final class Single implements PlantCoverage {
		private final BlockPos block;
		private final AxisAlignedBB bounds;

		private Single(BlockPos block) {
			this.block = block;
			this.bounds = new AxisAlignedBB(block);
		}

		@Override
		public boolean covers(BlockPos pos) {
			return this.block.equals(pos);
		}

		@Override
		public AxisAlignedBB asBounds() {
			return this.bounds;
		}

		@Override
		public BlockPos getOrigin() {
			return this.block;
		}

		@Override
		public BlockPos random(Random random) {
			return this.block;
		}

		@Override
		public Iterator<BlockPos> iterator() {
			return Iterators.singletonIterator(this.block);
		}
	}

	final class Set implements PlantCoverage {
		private final LongList blocks;
		private final BlockPos origin;
		private final AxisAlignedBB bounds;

		private Set(LongSet blocks, BlockPos origin) {
			this.blocks = new LongArrayList(blocks);
			this.origin = origin;

			AxisAlignedBB bounds = null;
			for (BlockPos pos : this) {
				AxisAlignedBB blockBounds = new AxisAlignedBB(pos);
				if (bounds == null) {
					bounds = blockBounds;
				} else {
					bounds = bounds.union(blockBounds);
				}
			}

			this.bounds = Preconditions.checkNotNull(bounds, "empty plant coverage");
		}

		@Override
		public boolean covers(BlockPos pos) {
			return this.blocks.contains(pos.toLong());
		}

		@Override
		public AxisAlignedBB asBounds() {
			return this.bounds;
		}

		@Override
		public BlockPos getOrigin() {
			return this.origin;
		}

		@Override
		public BlockPos random(Random random) {
			long pos = this.blocks.getLong(random.nextInt(blocks.size()));
			return new BlockPos(BlockPos.unpackX(pos), BlockPos.unpackY(pos), BlockPos.unpackZ(pos));
		}

		@Override
		public Iterator<BlockPos> iterator() {
			LongIterator blockIterator = this.blocks.iterator();

			return new Iterator<BlockPos>() {
				private final BlockPos.Mutable mutablePos = new BlockPos.Mutable();

				@Override
				public BlockPos next() {
					long pos = blockIterator.nextLong();
					return this.mutablePos.setPos(pos);
				}

				@Override
				public boolean hasNext() {
					return blockIterator.hasNext();
				}
			};
		}
	}
}
