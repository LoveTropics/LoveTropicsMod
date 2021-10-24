package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.placement;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantCoverage;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantPlacement;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.util.world.DelegatingSeedReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Block;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public final class PlaceFeaturePlantBehavior implements IGameBehavior {
	public static final Codec<PlaceFeaturePlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ConfiguredFeature.field_236264_b_.fieldOf("feature").forGetter(c -> c.feature)
	).apply(instance, PlaceFeaturePlantBehavior::new));

	private static final Tags.IOptionalNamedTag<Block> ROOTS = BlockTags.createOptional(new ResourceLocation("tropicraft", "roots"));
	private final Supplier<ConfiguredFeature<?, ?>> feature;

	public PlaceFeaturePlantBehavior(Supplier<ConfiguredFeature<?, ?>> feature) {
		this.feature = feature;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(BbPlantEvents.PLACE, (player, plot, pos) -> {
			ServerWorld world = game.getWorld();
			ConfiguredFeature<?, ?> tree = this.feature.get();
			Long2ObjectMap<BlockState> changedBlocks = this.generateFeature(world, plot, pos, tree);
			if (changedBlocks != null) {
				return this.buildPlacement(pos, changedBlocks);
			} else {
				return null;
			}
		});
	}

	@Nullable
	private Long2ObjectMap<BlockState> generateFeature(ServerWorld world, Plot plot, BlockPos pos, ConfiguredFeature<?, ?> feature) {
		if (feature.config instanceof BaseTreeFeatureConfig) {
			((BaseTreeFeatureConfig) feature.config).forcePlacement();
		}

		Long2ObjectMap<BlockState> changedBlocks = new Long2ObjectOpenHashMap<>();

		DelegatingSeedReader placementWorld = new DelegatingSeedReader(world) {
			@Override
			public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
				if (!isTreeBlock(state) || !plot.plants.canAddPlantAt(pos)) {
					return false;
				}

				if (this.getBlockState(pos) != state) {
					changedBlocks.put(pos.toLong(), state);
					return true;
				} else {
					return false;
				}
			}

			@Override
			public BlockState getBlockState(BlockPos pos) {
				BlockState changedBlock = changedBlocks.get(pos.toLong());
				if (changedBlock != null) {
					return changedBlock;
				} else {
					return super.getBlockState(pos);
				}
			}
		};

		ChunkGenerator chunkGenerator = world.getChunkProvider().getChunkGenerator();
		if (feature.generate(placementWorld, chunkGenerator, world.rand, pos)) {
			return changedBlocks;
		} else {
			return null;
		}
	}

	@Nullable
	private PlantPlacement buildPlacement(BlockPos origin, Long2ObjectMap<BlockState> changedBlocks) {
		LongSet coverage = new LongOpenHashSet();
		LongSet decorationCoverage = new LongOpenHashSet();
		for (Long2ObjectMap.Entry<BlockState> entry : Long2ObjectMaps.fastIterable(changedBlocks)) {
			long pos = entry.getLongKey();
			BlockState state = entry.getValue();
			if (!isDecorationBlock(state)) {
				coverage.add(pos);
			} else {
				decorationCoverage.add(pos);
			}
		}

		if (coverage.isEmpty()) {
			return null;
		}

		PlantPlacement placement = new PlantPlacement()
				.covers(PlantCoverage.of(coverage, origin));

		if (!decorationCoverage.isEmpty()) {
			placement.decorationCovers(PlantCoverage.of(decorationCoverage, origin));
		}

		return placement.places(world -> {
			BlockPos.Mutable pos = new BlockPos.Mutable();
			for (Long2ObjectMap.Entry<BlockState> entry : Long2ObjectMaps.fastIterable(changedBlocks)) {
				pos.setPos(entry.getLongKey());
				BlockState state = entry.getValue();
				world.setBlockState(pos, state, Constants.BlockFlags.DEFAULT | Constants.BlockFlags.UPDATE_NEIGHBORS);
			}
			return true;
		});
	}

	private static boolean isTreeBlock(BlockState state) {
		// Add stuff like vines and propagules as needed

		// TODO: beehives are above the floor and have a leaves block above them
		return state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.LEAVES) || state.isIn(ROOTS);
	}

	private static boolean isDecorationBlock(BlockState state) {
		return !state.isIn(BlockTags.LOGS);
	}
}
