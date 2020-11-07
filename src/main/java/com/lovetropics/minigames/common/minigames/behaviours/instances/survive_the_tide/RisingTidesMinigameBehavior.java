package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.minigames.behaviours.instances.PhasesMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RisingTidesMinigameBehavior implements IMinigameBehavior {
	private static final RegistryObject<Block> WATER_BARRIER = RegistryObject.of(new ResourceLocation("ltextras", "water_barrier"), ForgeRegistries.BLOCKS);

	// the maximum time in milliseconds that we should spend updating the tide per tick
	private static final long RISING_TIDE_THRESHOLD_MS = 15;

	private final String tideAreaKey;
	private final String icebergLinesKey;
	private final Map<String, Integer> phaseToTideHeight;
	private final List<String> phasesIcebergsGrow;
	private final int icebergGrowthTickRate;
	private int waterLevel;

	private MapRegion tideArea;
	private final List<IcebergLine> icebergLines = new ArrayList<>();

	private ChunkPos minTideChunk;
	private ChunkPos maxTideChunk;

	private final LongSet queuedChunksToUpdate = new LongOpenHashSet();
	private final Long2IntMap chunkWaterLevels = new Long2IntOpenHashMap();

	public RisingTidesMinigameBehavior(String tideAreaKey, String icebergLinesKey, final Map<String, Integer> phaseToTideHeight, final List<String> phasesIcebergsGrow, final int icebergGrowthTickRate) {
		this.tideAreaKey = tideAreaKey;
		this.icebergLinesKey = icebergLinesKey;
		this.phaseToTideHeight = phaseToTideHeight;
		this.phasesIcebergsGrow = phasesIcebergsGrow;
		this.icebergGrowthTickRate = icebergGrowthTickRate;
	}

	public static <T> RisingTidesMinigameBehavior parse(Dynamic<T> root) {
		final String tideAreaKey = root.get("tide_area_region").asString("tide_area");
		final String icebergLinesKey = root.get("iceberg_lines_region").asString("iceberg_lines");
		final Map<String, Integer> phaseToTideHeight = root.get("water_levels").asMap(
				key -> key.asString(""),
				value -> value.asInt(0)
		);
		final List<String> phasesIcebergsGrow = root.get("phases_icebergs_grow").asList(d -> d.asString(""));
		final int icebergGrowthTickRate = root.get("iceberg_growth_tick_rate").asInt(0);

		return new RisingTidesMinigameBehavior(tideAreaKey, icebergLinesKey, phaseToTideHeight, phasesIcebergsGrow, icebergGrowthTickRate);
	}

	@Override
	public ImmutableList<IMinigameBehaviorType<? extends IMinigameBehavior>> dependencies() {
		return ImmutableList.of(MinigameBehaviorTypes.PHASES.get());
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		tideArea = minigame.getMapRegions().getOne(tideAreaKey);

		minTideChunk = new ChunkPos(tideArea.min.getX() >> 4, tideArea.min.getZ() >> 4);
		maxTideChunk = new ChunkPos(tideArea.max.getX() >> 4, tideArea.max.getZ() >> 4);

		Random random = new Random();

		icebergLines.clear();
		for (MapRegion icebergLine : minigame.getMapRegions().get(icebergLinesKey)) {
			int startX = icebergLine.min.getX();
			int startZ = icebergLine.min.getZ();

			int endX = icebergLine.max.getX();
			int endZ = icebergLine.max.getZ();

			if (random.nextBoolean()) {
				int swap = startX;
				startX = endX;
				endX = swap;
			}

			if (random.nextBoolean()) {
				int swap = startZ;
				startZ = endZ;
				endZ = swap;
			}

			BlockPos start = new BlockPos(startX, 0, startZ);
			BlockPos end = new BlockPos(endX, 0, endZ);
			icebergLines.add(new IcebergLine(start, end, 10));
		}

		minigame.getOneBehavior(MinigameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
			waterLevel = phaseToTideHeight.get(phases.getFirstPhase().getKey());
			chunkWaterLevels.defaultReturnValue(waterLevel);
		});
	}

	@Override
	public void onLivingEntityUpdate(final IMinigameInstance minigame, LivingEntity entity) {
		// NOTE: DO NOT REMOVE THIS CHECK, CAUSES FISH TO DIE AND SPAWN ITEMS ON DEATH
		// FISH WILL KEEP SPAWNING, DYING AND COMPLETELY SLOW THE SERVER TO A CRAWL
		if (!entity.canBreatheUnderwater()) {
			if (entity.getPosY() <= this.waterLevel + 1 && entity.isInWater() && entity.ticksExisted % 20 == 0) {
				entity.attackEntityFrom(DamageSource.DROWN, 2.0F);
			}
		}
	}

	@Override
	public void worldUpdate(final IMinigameInstance minigame, World world) {
		minigame.getOneBehavior(MinigameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
			final PhasesMinigameBehavior.MinigamePhase phase = phases.getCurrentPhase();
			final int prevWaterLevel = phaseToTideHeight.get(phases.getPreviousPhase().orElse(phase).getKey());

			tickWaterLevel(minigame, phase, prevWaterLevel);

			if (phasesIcebergsGrow.contains(phase.getKey()) && minigame.ticks() % icebergGrowthTickRate == 0) {
				growIcebergs(world);
			}
		});

		processRisingTideQueue(minigame);
	}

	private void growIcebergs(final World world) {
		for (IcebergLine line : icebergLines) {
			line.generate(world, waterLevel);
		}
	}

	private int calculateWaterChangeInterval(int targetLevel, int prevLevel, int phaseLength) {
		int waterLevelDiff = prevLevel - targetLevel;
		return phaseLength / Math.max(1, Math.abs(waterLevelDiff));
	}

	private void processRisingTideQueue(IMinigameInstance minigame) {
		if (queuedChunksToUpdate.isEmpty()) {
			return;
		}

		ServerWorld world = minigame.getWorld();
		ServerChunkProvider chunkProvider = world.getChunkProvider();

		long startTime = System.currentTimeMillis();
		long updatedBlocks = 0;

		LongIterator iterator = queuedChunksToUpdate.iterator();
		while (iterator.hasNext()) {
			long chunkPos = iterator.nextLong();
			int chunkX = ChunkPos.getX(chunkPos);
			int chunkZ = ChunkPos.getZ(chunkPos);

			// we only want to apply updates to loaded chunks
			Chunk chunk = chunkProvider.getChunkWithoutLoading(chunkX, chunkZ);
			if (chunk == null) {
				continue;
			}

			iterator.remove();
			updatedBlocks += increaseTideForChunk(world, chunk);

			// limit the time we spend rising the tide each tick
			if (System.currentTimeMillis() - startTime > RISING_TIDE_THRESHOLD_MS) {
				break;
			}
		}

		if (updatedBlocks > 0) {
			long endTime = System.currentTimeMillis();
			LogManager.getLogger().info("Updated {} blocks in {}ms", updatedBlocks, endTime - startTime);
		}
	}

	private void tickWaterLevel(final IMinigameInstance minigame, final PhasesMinigameBehavior.MinigamePhase phase, final int prevWaterLevel) {
		final int targetWaterLevel = phaseToTideHeight.get(phase.getKey());

		int waterChangeInterval = this.calculateWaterChangeInterval(
				targetWaterLevel,
				prevWaterLevel,
				phase.getLengthInTicks()
		);

		if (waterLevel < targetWaterLevel && minigame.ticks() % waterChangeInterval == 0) {
			this.waterLevel++;

			// queue all the chunks to be updated!
			for (int z = minTideChunk.z; z <= maxTideChunk.z; z++) {
				for (int x = minTideChunk.x; x <= maxTideChunk.x; x++) {
					queuedChunksToUpdate.add(ChunkPos.asLong(x, z));
				}
			}
		}
	}

	// thicc boi
	private long increaseTideForChunk(World world, Chunk chunk) {
		ChunkPos chunkPos = chunk.getPos();

		int targetLevel = this.waterLevel;
		int lastLevel = this.chunkWaterLevels.put(chunkPos.asLong(), targetLevel);

		// no work to be done
		if (lastLevel >= targetLevel) {
			return 0;
		}

		// this is the total area over which we need to increase the tide
		BlockPos min = new BlockPos(tideArea.min.getX(), lastLevel, tideArea.min.getZ());
		BlockPos max = new BlockPos(tideArea.max.getX(), targetLevel, tideArea.max.getZ());

		long updatedBlocks = 0;

		ChunkSection[] sectionArray = chunk.getSections();

		int minChunkX = chunkPos.getXStart();
		int minChunkZ = chunkPos.getZStart();

		int minSection = lastLevel >> 4;
		int maxSection = targetLevel >> 4;

		BlockState waterBarrier = WATER_BARRIER.orElse(Blocks.BARRIER).getDefaultState();

		// iterate through all the sections that need to be changed
		for (int sectionY = minSection; sectionY <= maxSection; sectionY++) {
			ChunkSection section = sectionArray[sectionY];
			int minSectionY = sectionY << 4;

			// Calculate start/end within the current section
			BlockPos localMin = new BlockPos(
					Math.max(0, min.getX() - minChunkX),
					Math.max(0, min.getY() - minSectionY),
					Math.max(0, min.getZ() - minChunkZ)
			);

			BlockPos localMax = new BlockPos(
					Math.min(15, max.getX() - minChunkX),
					Math.min(15, max.getY() - minSectionY),
					Math.min(15, max.getZ() - minChunkZ)
			);

			// If this section is empty, we must add a new one
			if (section == Chunk.EMPTY_SECTION) {
				// This constructor expects the "base y" which means the real Y-level floored to the nearest multiple of 16
				// This is accomplished by removing the last 4 bits of the coordinate
				section = new ChunkSection(targetLevel & ~0xF);
				sectionArray[targetLevel >> 4] = section;
			}

			Heightmap heightmapSurface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE);
			Heightmap heightmapMotionBlocking = chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING);

			BlockPos.Mutable worldPos = new BlockPos.Mutable();

			for (BlockPos pos : BlockPos.getAllInBoxMutable(localMin, localMax)) {
				BlockState existing = section.getBlockState(pos.getX(), pos.getY(), pos.getZ());
				worldPos.setPos(minChunkX + pos.getX(), minSectionY + pos.getY(), minChunkZ + pos.getZ());

				BlockState toSet = null;
				if (existing.isAir(world, worldPos) || !existing.getMaterial().blocksMovement() || existing.getBlock() == Blocks.BAMBOO) {
					// If air or a replaceable block, just set to water
					toSet = Blocks.WATER.getDefaultState();
				} else if (existing.getBlock() instanceof IWaterLoggable) {
					// If waterloggable, set the waterloggable property to true
					toSet = existing.with(BlockStateProperties.WATERLOGGED, true);
					if (existing.getBlock() == Blocks.CAMPFIRE) {
						toSet = toSet.with(CampfireBlock.LIT, false);
					}
				} else if (existing.getBlock() == Blocks.BARRIER) {
					toSet = waterBarrier;
				}

				if (toSet != null) {
					if (existing.getBlock() == Blocks.BAMBOO) {
						world.setBlockState(worldPos, toSet, Constants.BlockFlags.NO_RERENDER | Constants.BlockFlags.BLOCK_UPDATE);
					} else {
						section.setBlockState(pos.getX(), pos.getY(), pos.getZ(), toSet);
					}

					// Tell the client about the change
					((ServerChunkProvider) world.getChunkProvider()).markBlockChanged(worldPos);
					// Update heightmap
					heightmapSurface.update(pos.getX(), worldPos.getY(), pos.getZ(), toSet);
					heightmapMotionBlocking.update(pos.getX(), worldPos.getY(), pos.getZ(), toSet);

					updatedBlocks++;
					// FIXES LIGHTING AT THE COST OF PERFORMANCE - TODO ask fry?
					// world.getChunkProvider().getLightManager().checkBlock(realPos);
				}
			}
		}

		if (updatedBlocks > 0) {
			// Make sure this chunk gets saved
			chunk.markDirty();
		}

		return updatedBlocks;
	}
}
