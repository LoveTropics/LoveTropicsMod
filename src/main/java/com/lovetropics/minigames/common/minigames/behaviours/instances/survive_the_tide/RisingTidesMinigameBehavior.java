package com.lovetropics.minigames.common.minigames.behaviours.instances.survive_the_tide;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.map.MapRegion;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.minigames.behaviours.instances.PhasesMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.common.util.Constants;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RisingTidesMinigameBehavior implements IMinigameBehavior
{
	private final String tideAreaKey;
	private final String icebergLinesKey;
	private final Map<String, Integer> phaseToTideHeight;
	private final List<String> phasesIcebergsGrow;
	private final int icebergGrowthTickRate;
	private int waterLevel;

	private MapRegion tideArea;
	private final List<IcebergLine> icebergLines = new ArrayList<>();

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
	public ImmutableList<IMinigameBehaviorType<?>> dependencies() {
		return ImmutableList.of(MinigameBehaviorTypes.PHASES.get());
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {
		tideArea = minigame.getMapRegions().getOne(tideAreaKey);

		icebergLines.clear();
		for (MapRegion icebergLine : minigame.getMapRegions().get(icebergLinesKey)) {
			icebergLines.add(new IcebergLine(icebergLine.min, icebergLine.max, 10));
		}
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
		minigame.getDefinition().getBehavior(MinigameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
			final PhasesMinigameBehavior.MinigamePhase phase = phases.getCurrentPhase();
			final int prevWaterLevel = phaseToTideHeight.get(phases.getPreviousPhase().orElse(phase).getKey());

			processWaterLevel(minigame, phase, prevWaterLevel);

			if (phasesIcebergsGrow.contains(phase.getKey()) && minigame.ticks() % icebergGrowthTickRate == 0) {
				growIcebergs(world);
			}
		});
	}

	@Override
	public void onStart(final IMinigameInstance minigame) {
		minigame.getDefinition().getBehavior(MinigameBehaviorTypes.PHASES.get()).ifPresent(phases -> {
			waterLevel = phaseToTideHeight.get(phases.getFirstPhase().getKey());
		});
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

	private void processWaterLevel(final IMinigameInstance minigame, final PhasesMinigameBehavior.MinigamePhase phase, final int prevWaterLevel) {
		final int targetWaterLevel = phaseToTideHeight.get(phase.getKey());

		int waterChangeInterval = this.calculateWaterChangeInterval(
				targetWaterLevel,
				prevWaterLevel,
				phase.getLengthInTicks()
		);

		if (waterLevel < targetWaterLevel && minigame.ticks() % waterChangeInterval == 0) {
			increaseTide(minigame.getWorld());
		}
	}

	private void increaseTide(final World world) {
		this.waterLevel++;
		BlockPos min = new BlockPos(tideArea.min.getX(), waterLevel, tideArea.min.getZ());
		BlockPos max = new BlockPos(tideArea.max.getX(), waterLevel, tideArea.max.getZ());
		ChunkPos minChunk = new ChunkPos(min);
		ChunkPos maxChunk = new ChunkPos(max);

		long startTime = System.currentTimeMillis();
		long updatedBlocks = 0;

		BlockPos.Mutable localStart = new BlockPos.Mutable();
		BlockPos.Mutable localEnd = new BlockPos.Mutable();
		BlockPos.Mutable realPos = new BlockPos.Mutable();

		for (int x = minChunk.x; x <= maxChunk.x; x++) {
			for (int z = minChunk.z; z <= maxChunk.z; z++) {
				ChunkPos chunkPos = new ChunkPos(x, z);
				BlockPos chunkStart = chunkPos.asBlockPos();
				// Extract current chunk section
				Chunk chunk = world.getChunk(x, z);
				ChunkSection[] sectionArray = chunk.getSections();
				ChunkSection section = sectionArray[this.waterLevel >> 4];
				int localY = this.waterLevel & 0xF;
				// Calculate start/end within the current section
				localStart.setPos(min.subtract(chunkStart));
				localStart.setPos(Math.max(0, localStart.getX()), localY, Math.max(0, localStart.getZ()));
				localEnd.setPos(max.subtract(chunkStart));
				localEnd.setPos(Math.min(15, localEnd.getX()), localY, Math.min(15, localEnd.getZ()));
				// If this section is empty, we must add a new one
				if (section == Chunk.EMPTY_SECTION) {
					// This constructor expects the "base y" which means the real Y-level floored to the nearest multiple of 16
					// This is accomplished by removing the last 4 bits of the coordinate
					section = new ChunkSection(this.waterLevel & ~0xF);
					sectionArray[this.waterLevel >> 4] = section;
				}
				Heightmap heightmapSurface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE);
				Heightmap heightmapMotionBlocking = chunk.getHeightmap(Heightmap.Type.MOTION_BLOCKING);
				boolean anyChanged = false;
				for (BlockPos pos : BlockPos.getAllInBoxMutable(localStart, localEnd)) {
					BlockState existing = section.getBlockState(pos.getX(), pos.getY(), pos.getZ());
					realPos.setPos(chunkStart.getX() + pos.getX(), this.waterLevel, chunkStart.getZ() + pos.getZ());
					BlockState toSet = null;
					if (existing.isAir(world, pos) || !existing.getMaterial().blocksMovement() || existing.getBlock() == Blocks.BAMBOO) {
						// If air or a replaceable block, just set to water
						toSet = Blocks.WATER.getDefaultState();
					} else if (existing.getBlock() instanceof IWaterLoggable) {
						// If waterloggable, set the waterloggable property to true
						toSet = existing.with(BlockStateProperties.WATERLOGGED, true);
						if (existing.getBlock() == Blocks.CAMPFIRE) {
							toSet = toSet.with(CampfireBlock.LIT, false);
						}
					}
					if (toSet != null) {
						anyChanged = true;
						if (existing.getBlock() == Blocks.BAMBOO) {
							world.setBlockState(realPos, toSet, Constants.BlockFlags.NO_RERENDER | Constants.BlockFlags.BLOCK_UPDATE);
						} else {
							section.setBlockState(pos.getX(), pos.getY(), pos.getZ(), toSet);
						}
						// Tell the client about the change
						((ServerChunkProvider)world.getChunkProvider()).markBlockChanged(realPos);
						// Update heightmap
						heightmapSurface.update(pos.getX(), realPos.getY(), pos.getZ(), toSet);
						heightmapMotionBlocking.update(pos.getX(), realPos.getY(), pos.getZ(), toSet);
						updatedBlocks++;
						// FIXES LIGHTING AT THE COST OF PERFORMANCE - TODO ask fry?
						// world.getChunkProvider().getLightManager().checkBlock(realPos);
					}
				}
				if (anyChanged) {
					// Make sure this chunk gets saved
					chunk.markDirty();
				}
			}
		}

		long endTime = System.currentTimeMillis();
		LogManager.getLogger().info("Updated {} blocks in {}ms", updatedBlocks, endTime - startTime);
	}
}
