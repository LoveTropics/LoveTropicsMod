package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.placement;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantCoverage;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantPlacement;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.util.world.DelegatingSeedReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.function.Predicate;
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
			Long2ObjectMap<BlockState> changedBlocks = this.generateFeature(world, pos, tree);
			if (changedBlocks != null) {
				return this.buildPlacement(pos, changedBlocks);
			} else {
				return null;
			}
		});
	}

	@Nullable
	private Long2ObjectMap<BlockState> generateFeature(ServerWorld world, BlockPos pos, ConfiguredFeature<?, ?> feature) {
		if (feature.config instanceof BaseTreeFeatureConfig) {
			((BaseTreeFeatureConfig) feature.config).forcePlacement();
		}

		BlockCapturingWorld capturingWorld = new BlockCapturingWorld(world, PlaceFeaturePlantBehavior::isTreeBlock);

		ChunkGenerator chunkGenerator = world.getChunkProvider().getChunkGenerator();
		if (feature.generate(capturingWorld, chunkGenerator, world.rand, pos)) {
			return capturingWorld.getCapturedBlocks();
		} else {
			return null;
		}
	}

	@Nullable
	private PlantPlacement buildPlacement(BlockPos origin, Long2ObjectMap<BlockState> blocks) {
		LongSet coverage = new LongOpenHashSet();
		LongSet decorationCoverage = new LongOpenHashSet();

		for (Long2ObjectMap.Entry<BlockState> entry : Long2ObjectMaps.fastIterable(blocks)) {
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

		return placement.places((world, finalCoverage) -> {
			BlockPos.Mutable pos = new BlockPos.Mutable();
			for (Long2ObjectMap.Entry<BlockState> entry : Long2ObjectMaps.fastIterable(blocks)) {
				pos.setPos(entry.getLongKey());
				if (finalCoverage.covers(pos)) {
					BlockState state = entry.getValue();
					world.setBlockState(pos, state, Constants.BlockFlags.DEFAULT | Constants.BlockFlags.UPDATE_NEIGHBORS);
				}
			}
			return true;
		});
	}

	private static boolean isTreeBlock(BlockState state) {
		return state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.LEAVES) || state.isIn(ROOTS);
	}

	private static boolean isDecorationBlock(BlockState state) {
		return !state.isIn(BlockTags.LOGS);
	}

	static class BlockCapturingWorld extends DelegatingSeedReader {
		private final Long2ObjectMap<BlockState> simulatedBlocks = new Long2ObjectOpenHashMap<>();
		private final Long2ObjectMap<BlockState> capturedBlocks = new Long2ObjectOpenHashMap<>();

		private final Predicate<BlockState> filter;

		BlockCapturingWorld(ISeedReader parent, Predicate<BlockState> filter) {
			super(parent);
			this.filter = filter;
		}

		Long2ObjectMap<BlockState> getCapturedBlocks() {
			return capturedBlocks;
		}

		@Override
		public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
			BlockState oldState = this.getBlockState(pos);
			if (oldState == state) {
				return false;
			}

			long posKey = pos.toLong();
			if (!state.equals(super.getBlockState(pos))) {
				simulatedBlocks.put(posKey, state);
				if (filter.test(state)) {
					capturedBlocks.put(posKey, state);
				} else {
					capturedBlocks.remove(posKey);
				}
			} else {
				simulatedBlocks.remove(posKey);
				capturedBlocks.remove(posKey);
			}

			return true;
		}

		@Override
		public BlockState getBlockState(BlockPos pos) {
			BlockState changedBlock = simulatedBlocks.get(pos.toLong());
			if (changedBlock != null) {
				return changedBlock;
			} else {
				return super.getBlockState(pos);
			}
		}
	}
}
