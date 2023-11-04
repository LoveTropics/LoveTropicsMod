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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class TideFiller {
	private static final RegistryObject<Block> WATER_BARRIER = RegistryObject.create(new ResourceLocation("ltextras", "water_barrier"), ForgeRegistries.BLOCKS);
	private static final RegistryObject<Block> SAND_LAYER = RegistryObject.create(new ResourceLocation("weather2", "sand_layer"), ForgeRegistries.BLOCKS);

	public static long fillChunk(int minX, int minZ, int maxX, int maxZ, LevelChunk chunk, int fromY, int toY) {
		Level world = chunk.getLevel();
		ServerChunkCache chunkProvider = (ServerChunkCache) world.getChunkSource();

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
			LevelChunkSection section = chunk.getSection(world.getSectionIndexFromSectionY(sectionY));
			int minSectionY = SectionPos.sectionToBlockCoord(sectionY);
			int maxSectionY = minSectionY + SectionPos.SECTION_SIZE - 1;

			// Calculate start/end within the current section
			BlockPos sectionMin = new BlockPos(chunkMin.getX(), Math.max(chunkMin.getY(), minSectionY), chunkMin.getZ());
			BlockPos sectionMax = new BlockPos(chunkMax.getX(), Math.min(chunkMax.getY(), maxSectionY), chunkMax.getZ());

			for (BlockPos worldPos : BlockPos.betweenClosed(sectionMin, sectionMax)) {
				int localX = SectionPos.sectionRelative(worldPos.getX());
				int localY = SectionPos.sectionRelative(worldPos.getY());
				int localZ = SectionPos.sectionRelative(worldPos.getZ());

				BlockState existingBlock = section.getBlockState(localX, localY, localZ);

				BlockState newBlock = mapBlock(existingBlock, worldPos.getY(), fromY);
				if (newBlock == null) continue;

				if (existingBlock.getBlock() != Blocks.BAMBOO) {
					section.setBlockState(localX, localY, localZ, newBlock);
				} else {
					world.setBlock(worldPos, newBlock, Block.UPDATE_INVISIBLE | Block.UPDATE_CLIENTS);
				}

				// Update heightmap
				heightmapSurface.update(localX, localY, localZ, newBlock);
				heightmapMotionBlocking.update(localX, localY, localZ, newBlock);

				// Tell the client about the change
				chunkProvider.blockChanged(worldPos);
				chunkProvider.getLightEngine().checkBlock(worldPos);

				updatedBlocks++;
			}
		}

		if (updatedBlocks > 0) {
			// Make sure this chunk gets saved
			chunk.setUnsaved(true);
		}

		return updatedBlocks;
	}

	@Nullable
	private static BlockState mapBlock(BlockState state, int y, int fromWaterLevel) {
		if (y <= fromWaterLevel) {
			return mapBlockBelowWater(state);
		} else {
			return mapBlockRisingWater(state);
		}
	}

	@Nullable
	private static BlockState mapBlockRisingWater(BlockState state) {
		Block block = state.getBlock();

		if (state.isAir() || !state.blocksMotion() || block == Blocks.BAMBOO || is(state, SAND_LAYER)) {
			// If air or a replaceable block, just set to water
			return Blocks.WATER.defaultBlockState();
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

		return null;
	}

	private static boolean is(BlockState state, RegistryObject<Block> block) {
		return block.isPresent() && block.get() == state.getBlock();
	}

	@Nullable
	private static BlockState mapBlockBelowWater(BlockState state) {
		if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT_PATH) {
			return Blocks.DIRT.defaultBlockState();
		}

		return null;
	}
}
