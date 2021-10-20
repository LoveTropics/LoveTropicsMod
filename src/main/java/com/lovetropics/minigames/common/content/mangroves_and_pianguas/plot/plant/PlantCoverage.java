package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Iterator;

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

	AxisAlignedBB asBounds();

	BlockPos getOrigin();

	default boolean intersects(PlantCoverage other) {
		if (!this.asBounds().intersects(other.asBounds())) {
			return false;
		}

		for (BlockPos pos : this) {
			if (other.covers(pos)) {
				return true;
			}
		}

		return false;
	}

	@Nullable
	default PlantCoverage removeIntersection(PlantCoverage other) {
		if (!this.intersects(other)) {
			return this;
		}

		LongSet blocks = new LongOpenHashSet();
		for (BlockPos pos : this) {
			if (!other.covers(pos)) {
				blocks.add(pos.toLong());
			}
		}

		return !blocks.isEmpty() ? new Set(blocks, this.getOrigin()) : null;
	}

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
		public BlockPos getOrigin() {
			return this.block;
		}

		@Override
		public Iterator<BlockPos> iterator() {
			return Iterators.singletonIterator(this.block);
		}
	}

	final class Set implements PlantCoverage {
		private final LongSet blocks;
		private final BlockPos origin;
		private final AxisAlignedBB bounds;

		Set(LongSet blocks, BlockPos origin) {
			this.blocks = blocks;
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
