package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant;

import com.lovetropics.lib.codec.CodecRegistry;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.blockplacer.DoublePlantBlockPlacer;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Supplier;

public interface PlantPlacement {
	CodecRegistry<String, Codec<? extends PlantPlacement>> REGISTRY = CodecRegistry.stringKeys();
	Codec<PlantPlacement> CODEC = REGISTRY.dispatchStable(PlantPlacement::getCodec, Function.identity());

	static void register() {
		REGISTRY.register("single", SingleBlock.CODEC);
		REGISTRY.register("double", DoubleBlock.CODEC);
		REGISTRY.register("tree", Tree.CODEC);
	}

	@Nullable
	PlantCoverage place(ServerWorld world, Plot plot, BlockPos pos);

	Codec<? extends PlantPlacement> getCodec();

	final class SingleBlock implements PlantPlacement {
		public static final Codec<SingleBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				MoreCodecs.BLOCK_STATE.fieldOf("block").forGetter(c -> c.block)
		).apply(instance, SingleBlock::new));

		private final BlockState block;

		public SingleBlock(BlockState block) {
			this.block = block;
		}

		@Override
		public PlantCoverage place(ServerWorld world, Plot plot, BlockPos pos) {
			world.setBlockState(pos, this.block);
			return PlantCoverage.of(pos);
		}

		@Override
		public Codec<? extends PlantPlacement> getCodec() {
			return CODEC;
		}
	}

	final class DoubleBlock implements PlantPlacement {
		public static final Codec<DoubleBlock> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				MoreCodecs.BLOCK_STATE.fieldOf("block").forGetter(c -> c.block)
		).apply(instance, DoubleBlock::new));

		private final BlockState block;

		public DoubleBlock(BlockState block) {
			this.block = block;
		}

		@Override
		public PlantCoverage place(ServerWorld world, Plot plot, BlockPos pos) {
			DoublePlantBlockPlacer.PLACER.place(world, pos, this.block, world.rand);
			return PlantCoverage.ofDouble(pos);
		}

		@Override
		public Codec<? extends PlantPlacement> getCodec() {
			return CODEC;
		}
	}

	final class Tree implements PlantPlacement {
		public static final Codec<Tree> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				ConfiguredFeature.field_236264_b_.fieldOf("tree").forGetter(c -> c.tree)
		).apply(instance, Tree::new));

		private static final Direction[] DIRECTIONS = Direction.values();

		private final Supplier<ConfiguredFeature<?, ?>> tree;

		public Tree(Supplier<ConfiguredFeature<?, ?>> tree) {
			this.tree = tree;
		}

		@Override
		public PlantCoverage place(ServerWorld world, Plot plot, BlockPos pos) {
			ConfiguredFeature<?, ?> tree = this.tree.get();
			if (this.generateTree(world, pos, tree)) {
				return this.buildCoverage(world, plot, pos);
			} else {
				return null;
			}
		}

		private boolean generateTree(ServerWorld world, BlockPos pos, ConfiguredFeature<?, ?> tree) {
			if (tree.config instanceof BaseTreeFeatureConfig) {
				((BaseTreeFeatureConfig) tree.config).forcePlacement();
			}

			BlockState saplingState = world.getBlockState(pos);
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), Constants.BlockFlags.NO_RERENDER);

			ChunkGenerator chunkGenerator = world.getChunkProvider().getChunkGenerator();
			if (tree.generate(world, chunkGenerator, world.rand, pos)) {
				return true;
			} else {
				world.setBlockState(pos, saplingState, Constants.BlockFlags.NO_RERENDER);
				return false;
			}
		}

		@Nullable
		private PlantCoverage buildCoverage(ServerWorld world, Plot plot, BlockPos pos) {
			// iterate all new tree blocks
			LongSet blocks = new LongOpenHashSet();

			Deque<BlockPos> queue = new LinkedList<>();
			queue.add(pos.toImmutable());

			// DFS new blocks from trees
			while (!queue.isEmpty()) {
				BlockPos poll = queue.poll();

				// TODO: prioritize trunk blocks so trunks are never cut off

				// DFS more if this is a tree block and it's not an already globally tracked tree or a part of this current tree that we've already seen
				if (isTreeBlock(world.getBlockState(poll)) && !plot.plants.hasPlantAt(poll) && !blocks.contains(poll.toLong())) {
					blocks.add(poll.toLong());

					// Go forth in all directions
					for (Direction value : DIRECTIONS) {
						queue.add(poll.offset(value));
					}
				}
			}

			if (blocks.isEmpty()) {
				return null;
			}

			return PlantCoverage.of(blocks, pos);
		}

		@Override
		public Codec<? extends PlantPlacement> getCodec() {
			return CODEC;
		}

		private static boolean isTreeBlock(BlockState state) {
			// Add stuff like vines and propagules as needed

			// TODO: beehives are above the floor and have a leaves block above them
			return BlockTags.LOGS.contains(state.getBlock()) || BlockTags.LEAVES.contains(state.getBlock());
		}
	}
}
