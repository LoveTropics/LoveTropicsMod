package com.lovetropics.minigames.common.content.survive_the_tide;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
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
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class TideFiller {
	private static final BlockState WATER = Blocks.WATER.defaultBlockState();
	private static final RegistryObject<Block> WATER_BARRIER = RegistryObject.create(new ResourceLocation("ltextras", "water_barrier"), ForgeRegistries.BLOCKS);
	private static final RegistryObject<Block> SAND_LAYER = RegistryObject.create(new ResourceLocation("weather2", "sand_layer"), ForgeRegistries.BLOCKS);

	public static long fillChunk(int minX, int minZ, int maxX, int maxZ, LevelChunk chunk, int fromY, int toY) {
		Level level = chunk.getLevel();
		ServerChunkCache chunkProvider = (ServerChunkCache) level.getChunkSource();
		LevelLightEngine lightEngine = chunkProvider.getLightEngine();

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

			if (section.hasOnlyAir()) {
				lightEngine.updateSectionStatus(SectionPos.of(chunkPos.x, sectionY, chunkPos.z), false);
			}

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

				chunkProvider.blockChanged(worldPos);

				if (newBlock == WATER || LightEngine.hasDifferentLightProperties(chunk, worldPos, existingBlock, newBlock)) {
					chunk.getSkyLightSources().update(chunk, localX, worldY, localZ);
					lightEngine.checkBlock(worldPos);
				}

				updatedBlocks++;
			}
		}

		if (updatedBlocks > 0) {
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
			return WATER_BARRIER.orElse(Blocks.BARRIER).defaultBlockState();
		}

		if (block == Blocks.BLACK_CONCRETE_POWDER) {
			// adding to the amazing list of hardcoded replacements.. yes!
			return Blocks.BLACK_CONCRETE.defaultBlockState();
		}

		return state;
	}

	private static boolean is(BlockState state, RegistryObject<Block> block) {
		return block.isPresent() && block.get() == state.getBlock();
	}

	private static BlockState mapBlockBelowWater(BlockState state) {
		if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT_PATH) {
			return Blocks.DIRT.defaultBlockState();
		}
		return state;
	}
}
