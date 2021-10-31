package com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant;

import java.util.Iterator;
import java.util.Random;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

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

	static PlantCoverage or(PlantCoverage left, PlantCoverage right) {
		return new Or(left, right);
	}

	boolean covers(BlockPos pos);

	BlockPos random(Random random);

	AxisAlignedBB asBounds();

	BlockPos getOrigin();
	
	default void add(BlockPos pos) {
		throw new UnsupportedOperationException("This coverage type cannot be added to");
	}

	default Stream<BlockPos> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

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
	default PlantCoverage removeIntersection(LongSet intersection) {
		LongSet blocks = new LongOpenHashSet();
		for (BlockPos pos : this) {
			if (!intersection.contains(pos.toLong())) {
				blocks.add(pos.toLong());
			}
		}

		return !blocks.isEmpty() ? new Set(blocks, this.getOrigin()) : null;
	}

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
		public void add(BlockPos pos) {
			if (this.bounds.contains(Vector3d.copyCentered(pos))) {
				this.blocks.add(pos.toLong());
			} else {
				throw new IllegalArgumentException("Position not within bounds");
			}
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

	final class Or implements PlantCoverage {
		private final PlantCoverage left;
		private final PlantCoverage right;
		private final AxisAlignedBB bounds;

		Or(PlantCoverage left, PlantCoverage right) {
			this.left = left;
			this.right = right;
			this.bounds = left.asBounds().union(right.asBounds());
		}

		@Override
		public boolean covers(BlockPos pos) {
			return this.left.covers(pos) || this.right.covers(pos);
		}

		@Override
		public BlockPos random(Random random) {
			return random.nextBoolean() ? this.left.random(random) : this.right.random(random);
		}

		@Override
		public AxisAlignedBB asBounds() {
			return this.bounds;
		}

		@Override
		public BlockPos getOrigin() {
			return this.left.getOrigin();
		}

		@Override
		public Iterator<BlockPos> iterator() {
			return Iterators.concat(this.left.iterator(), this.right.iterator());
		}
	}

	final class Builder {
		private final LongSet blocks = new LongOpenHashSet();
		private BlockPos origin;

		public Builder add(BlockPos pos) {
			this.blocks.add(pos.toLong());
			if (this.origin == null) {
				this.origin = pos;
			}
			return this;
		}

		public Builder setOrigin(BlockPos pos) {
			this.origin = pos;
			return this;
		}

		public PlantCoverage build() {
			if (this.blocks.isEmpty() || this.origin == null) {
				throw new IllegalStateException("cannot build empty plant");
			}

			if (this.blocks.size() == 1) {
				return PlantCoverage.of(BlockPos.fromLong(blocks.iterator().nextLong()));
			} else {
				return PlantCoverage.of(this.blocks, this.origin);
			}
		}
	}
}
