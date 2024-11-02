package com.lovetropics.minigames.common.content.survive_the_tide;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.neoforged.neoforge.registries.DeferredHolder;

public class TideFiller {
	private static final BlockState WATER = Blocks.WATER.defaultBlockState();
	private static final DeferredHolder<Block, Block> WATER_BARRIER = DeferredHolder.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("ltextras", "water_barrier"));
	private static final DeferredHolder<Block, Block> SAND_LAYER = DeferredHolder.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("weather2", "sand_layer"));

	public static long fillChunk(int minX, int minZ, int maxX, int maxZ, LevelChunk chunk, int fromY, int toY) {
		Level level = chunk.getLevel();
		LevelLightEngine lightEngine = level.getLightEngine();

		ChunkPos chunkPos = chunk.getPos();

		Heightmap heightmapSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE);
		Heightmap heightmapMotionBlocking = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING);

		// this is the total area over which we need to increase the tide
		BlockPos chunkMin = new BlockPos(
				Math.max(minX, chunkPos.getMinBlockX()),
				fromY,
				Math.max(minZ, chunkPos.getMinBlockZ())
		);
		BlockPos chunkMax = new BlockPos(
				Math.min(maxX, chunkPos.getMaxBlockX()),
				toY,
				Math.min(maxZ, chunkPos.getMaxBlockZ())
		);

		long updatedBlocks = 0;

		int fromSection = SectionPos.blockToSectionCoord(fromY);
		int toSection = SectionPos.blockToSectionCoord(toY);

		// iterate through all the sections that need to be changed
		for (int sectionY = fromSection; sectionY <= toSection; sectionY++) {
			LevelChunkSection section = chunk.getSection(level.getSectionIndexFromSectionY(sectionY));
			int minSectionY = SectionPos.sectionToBlockCoord(sectionY);
			int maxSectionY = minSectionY + SectionPos.SECTION_SIZE - 1;

			// Calculate start/end within the current section
			BlockPos sectionMin = new BlockPos(chunkMin.getX(), Math.max(chunkMin.getY(), minSectionY), chunkMin.getZ());
			BlockPos sectionMax = new BlockPos(chunkMax.getX(), Math.min(chunkMax.getY(), maxSectionY), chunkMax.getZ());

			// Don't actually trigger light updates, but make sure the light engine has the information it needs if a block update does happen
			if (section.hasOnlyAir()) {
				lightEngine.updateSectionStatus(SectionPos.of(chunkPos.x, sectionY, chunkPos.z), false);
			}

			boolean changed = false;

			for (BlockPos worldPos : BlockPos.betweenClosed(sectionMin, sectionMax)) {
				int worldY = worldPos.getY();
				int localX = SectionPos.sectionRelative(worldPos.getX());
				int localY = SectionPos.sectionRelative(worldY);
				int localZ = SectionPos.sectionRelative(worldPos.getZ());

				BlockState existingBlock = section.getBlockState(localX, localY, localZ);

				BlockState newBlock = mapBlock(existingBlock, worldY, fromY);
				if (newBlock == existingBlock) {
					continue;
				}

				if (existingBlock.getBlock() != Blocks.BAMBOO) {
					section.setBlockState(localX, localY, localZ, newBlock);
				} else {
					level.setBlock(worldPos, newBlock, Block.UPDATE_INVISIBLE | Block.UPDATE_CLIENTS);
				}

				heightmapSurface.update(localX, worldY, localZ, newBlock);
				heightmapMotionBlocking.update(localX, worldY, localZ, newBlock);

				updatedBlocks++;
				changed = true;
			}

			if (changed && level.isClientSide) {
				markSectionForRerender(chunkPos.x, sectionY, chunkPos.z);
			}
		}

		if (updatedBlocks > 0 && !level.isClientSide) {
			// Make sure this chunk gets saved
			chunk.setUnsaved(true);
		}

		return updatedBlocks;
	}

	private static BlockState mapBlock(BlockState state, int y, int fromWaterLevel) {
		if (y <= fromWaterLevel) {
			return mapBlockBelowWater(state);
		} else {
			return mapBlockRisingWater(state);
		}
	}

	private static BlockState mapBlockRisingWater(BlockState state) {
		Block block = state.getBlock();

		if (state.isAir() || !state.blocksMotion() || block == Blocks.BAMBOO || is(state, SAND_LAYER)) {
			return WATER;
		}

		if (block instanceof SimpleWaterloggedBlock) {
			// If waterloggable, set the waterloggable property to true
			state = state.setValue(BlockStateProperties.WATERLOGGED, true);
			if (block == Blocks.CAMPFIRE) {
				state = state.setValue(CampfireBlock.LIT, false);
			}
			return state;
		}

		if (block == Blocks.BARRIER) {
			return (WATER_BARRIER.isBound() ? WATER_BARRIER.value() : Blocks.BARRIER).defaultBlockState();
		}

		if (block == Blocks.BLACK_CONCRETE_POWDER) {
			// adding to the amazing list of hardcoded replacements.. yes!
			return Blocks.BLACK_CONCRETE.defaultBlockState();
		}

		return state;
	}

	private static boolean is(BlockState state, DeferredHolder<Block, Block> block) {
		// If the block doesn't exist, calling is() will throw
		if (!block.isBound()) {
			return false;
		}
		return state.is(block);
	}

	private static BlockState mapBlockBelowWater(BlockState state) {
		if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT_PATH) {
			return Blocks.DIRT.defaultBlockState();
		}
		return state;
	}

	private static void markSectionForRerender(int sectionX, int sectionY, int sectionZ) {
		final LevelRenderer levelRenderer = Minecraft.getInstance().levelRenderer;
		levelRenderer.setSectionDirtyWithNeighbors(sectionX, sectionY, sectionZ);
	}
}
