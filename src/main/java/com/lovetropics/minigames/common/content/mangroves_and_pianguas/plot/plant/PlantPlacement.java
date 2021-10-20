package com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant;

import com.lovetropics.lib.codec.CodecRegistry;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.util.world.DelegatingSeedReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.blockplacer.DoublePlantBlockPlacer;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.function.Function;
import java.util.function.LongPredicate;
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

		private final Supplier<ConfiguredFeature<?, ?>> tree;

		public Tree(Supplier<ConfiguredFeature<?, ?>> tree) {
			this.tree = tree;
		}

		@Override
		public PlantCoverage place(ServerWorld world, Plot plot, BlockPos pos) {
			ConfiguredFeature<?, ?> tree = this.tree.get();
			LongSet changedBlocks = this.generateTree(world, pos, tree);
			if (changedBlocks != null) {
				return this.buildCoverage(world, plot, changedBlocks);
			} else {
				return null;
			}
		}

		@Nullable
		private LongSet generateTree(ServerWorld world, BlockPos pos, ConfiguredFeature<?, ?> tree) {
			if (tree.config instanceof BaseTreeFeatureConfig) {
				((BaseTreeFeatureConfig) tree.config).forcePlacement();
			}

			BlockState saplingState = world.getBlockState(pos);
			world.setBlockState(pos, Blocks.AIR.getDefaultState(), Constants.BlockFlags.NO_RERENDER);

			LongSet changedBlocks = new LongOpenHashSet();

			DelegatingSeedReader placementWorld = new DelegatingSeedReader(world) {
				@Override
				public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
					if (super.setBlockState(pos, state, flags, recursionLeft)) {
						changedBlocks.add(pos.toLong());
						return true;
					} else {
						return false;
					}
				}
			};

			ChunkGenerator chunkGenerator = world.getChunkProvider().getChunkGenerator();
			if (tree.generate(placementWorld, chunkGenerator, world.rand, pos)) {
				return changedBlocks;
			} else {
				world.setBlockState(pos, saplingState, Constants.BlockFlags.NO_RERENDER);
				return null;
			}
		}

		@Nullable
		private PlantCoverage buildCoverage(ServerWorld world, Plot plot, LongSet changedBlocks) {
			LongSet coverage = new LongOpenHashSet(changedBlocks);

			BlockPos.Mutable pos = new BlockPos.Mutable();
			coverage.removeIf((LongPredicate) packedPos -> {
				pos.setPos(packedPos);

				BlockState state = world.getBlockState(pos);
				if (!isTreeBlock(state)) return true;

				return plot.plants.hasPlantAt(pos) && !state.isIn(BlockTags.LOGS);
			});

			if (!coverage.isEmpty()) {
				return PlantCoverage.of(coverage, pos);
			} else {
				return null;
			}
		}

		@Override
		public Codec<? extends PlantPlacement> getCodec() {
			return CODEC;
		}

		private static boolean isTreeBlock(BlockState state) {
			// Add stuff like vines and propagules as needed

			// TODO: beehives are above the floor and have a leaves block above them
			return state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.LEAVES);
		}
	}
}
