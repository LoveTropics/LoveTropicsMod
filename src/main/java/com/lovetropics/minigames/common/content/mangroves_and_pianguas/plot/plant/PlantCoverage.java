package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.Iterator;

public interface PlantCoverage extends Iterable<BlockPos> {
	static PlantCoverage of(BlockPos block) {
		return new Single(block);
	}

	static PlantCoverage of(LongSet blocks) {
		return new Set(blocks);
	}

	boolean covers(BlockPos pos);

	AxisAlignedBB asBounds();

	final class Single implements PlantCoverage {
		private final BlockPos block;
		private final AxisAlignedBB bounds;

		Single(BlockPos block) {
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
		public Iterator<BlockPos> iterator() {
			return Iterators.singletonIterator(this.block);
		}
	}

	final class Set implements PlantCoverage {
		private final LongSet blocks;
		private final AxisAlignedBB bounds;

		Set(LongSet blocks) {
			this.blocks = blocks;

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
