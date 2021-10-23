package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant.placement;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpPlantEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantCoverage;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.util.world.DelegatingSeedReader;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.BaseTreeFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.function.LongPredicate;
import java.util.function.Supplier;

public final class PlaceFeaturePlantBehavior implements IGameBehavior {
	public static final Codec<PlaceFeaturePlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ConfiguredFeature.field_236264_b_.fieldOf("feature").forGetter(c -> c.feature)
	).apply(instance, PlaceFeaturePlantBehavior::new));

	private final Supplier<ConfiguredFeature<?, ?>> feature;

	public PlaceFeaturePlantBehavior(Supplier<ConfiguredFeature<?, ?>> feature) {
		this.feature = feature;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(MpPlantEvents.PLACE, (player, plot, pos) -> {
			ServerWorld world = game.getWorld();
			ConfiguredFeature<?, ?> tree = this.feature.get();
			LongSet changedBlocks = this.generateFeature(world, plot, pos, tree);
			if (changedBlocks != null) {
				return this.buildCoverage(world, plot, changedBlocks);
			} else {
				return null;
			}
		});
	}

	@Nullable
	private LongSet generateFeature(ServerWorld world, Plot plot, BlockPos pos, ConfiguredFeature<?, ?> feature) {
		if (feature.config instanceof BaseTreeFeatureConfig) {
			((BaseTreeFeatureConfig) feature.config).forcePlacement();
		}

		LongSet changedBlocks = new LongOpenHashSet();

		DelegatingSeedReader placementWorld = new DelegatingSeedReader(world) {
			@Override
			public boolean setBlockState(BlockPos pos, BlockState state, int flags, int recursionLeft) {
				if (!plot.bounds.contains(pos) || plot.plants.getPlantAt(pos) != null) {
					return false;
				}

				if (super.setBlockState(pos, state, flags, recursionLeft)) {
					changedBlocks.add(pos.toLong());
					return true;
				} else {
					return false;
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

	private static boolean isTreeBlock(BlockState state) {
		// Add stuff like vines and propagules as needed

		// TODO: beehives are above the floor and have a leaves block above them
		return state.isIn(BlockTags.LOGS) || state.isIn(BlockTags.LEAVES);
	}
}
