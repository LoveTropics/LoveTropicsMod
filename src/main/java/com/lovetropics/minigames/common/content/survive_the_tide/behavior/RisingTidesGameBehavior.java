package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.survive_the_tide.IcebergLine;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.GamePhase;
import com.lovetropics.minigames.common.core.game.state.GamePhaseState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SSpawnParticlePacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
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

import javax.annotation.Nullable;
import java.util.*;

public class RisingTidesGameBehavior implements IGameBehavior {
	public static final Codec<RisingTidesGameBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.STRING.optionalFieldOf("tide_area_region", "tide_area").forGetter(c -> c.tideAreaKey),
				Codec.STRING.optionalFieldOf("iceberg_lines_region", "iceberg_lines").forGetter(c -> c.icebergLinesKey),
				Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("water_levels").forGetter(c -> c.phaseToTideHeight),
				Codec.STRING.listOf().fieldOf("phases_icebergs_grow").forGetter(c -> new ArrayList<>(c.phasesIcebergsGrow)),
				Codec.INT.fieldOf("iceberg_growth_tick_rate").forGetter(c -> c.icebergGrowthTickRate)
		).apply(instance, RisingTidesGameBehavior::new);
	});

	private static final RegistryObject<Block> WATER_BARRIER = RegistryObject.of(new ResourceLocation("ltextras", "water_barrier"), ForgeRegistries.BLOCKS);

	// the maximum time in milliseconds that we should spend updating the tide per tick
	private static final int HIGH_PRIORITY_BUDGET_PER_TICK = 150;
	private static final int LOW_PRIORITY_BUDGET_PER_TICK = 10;

	private static final int HIGH_PRIORITY_DISTANCE_2 = 64 * 64;

	private final String tideAreaKey;
	private final String icebergLinesKey;
	private final Map<String, Integer> phaseToTideHeight;
	private final Set<String> phasesIcebergsGrow;
	private final int icebergGrowthTickRate;
	private int waterLevel;

	private BlockBox tideArea;
	private final List<IcebergLine> icebergLines = new ArrayList<>();

	private ChunkPos minTideChunk;
	private ChunkPos maxTideChunk;

	private final LongSet highPriorityUpdates = new LongLinkedOpenHashSet();
	private final LongSet lowPriorityUpdates = new LongLinkedOpenHashSet();
	private final Long2IntMap chunkWaterLevels = new Long2IntOpenHashMap();

	private GamePhaseState phases;
	private GamePhase lastPhase;

	public RisingTidesGameBehavior(String tideAreaKey, String icebergLinesKey, final Map<String, Integer> phaseToTideHeight, final List<String> phasesIcebergsGrow, final int icebergGrowthTickRate) {
		this.tideAreaKey = tideAreaKey;
		this.icebergLinesKey = icebergLinesKey;
		this.phaseToTideHeight = phaseToTideHeight;
		this.phasesIcebergsGrow = new ObjectOpenHashSet<>(phasesIcebergsGrow);
		this.icebergGrowthTickRate = icebergGrowthTickRate;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		tideArea = game.getMapRegions().getOrThrow(tideAreaKey);

		minTideChunk = new ChunkPos(tideArea.min.getX() >> 4, tideArea.min.getZ() >> 4);
		maxTideChunk = new ChunkPos(tideArea.max.getX() >> 4, tideArea.max.getZ() >> 4);

		Random random = new Random();

		icebergLines.clear();
		for (BlockBox icebergLine : game.getMapRegions().get(icebergLinesKey)) {
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

		phases = game.getState().getOrThrow(GamePhaseState.KEY);

		events.listen(GamePhaseEvents.START, () -> {
			lastPhase = phases.get();
			waterLevel = phaseToTideHeight.get(phases.get().key);
			chunkWaterLevels.defaultReturnValue(waterLevel);
		});

		events.listen(GameLivingEntityEvents.TICK, this::onLivingEntityUpdate);
		events.listen(GamePhaseEvents.TICK, () -> tick(game));

		events.listen(GameLogicEvents.PHASE_CHANGE, (phase, lastPhase) -> {
			this.lastPhase = lastPhase;
		});
	}

	private void onLivingEntityUpdate(LivingEntity entity) {
		// NOTE: DO NOT REMOVE THIS CHECK, CAUSES FISH TO DIE AND SPAWN ITEMS ON DEATH
		// FISH WILL KEEP SPAWNING, DYING AND COMPLETELY SLOW THE SERVER TO A CRAWL
		if (!entity.canBreatheUnderwater()) {
			if (entity.getY() <= this.waterLevel + 1 && entity.isInWater() && entity.tickCount % 20 == 0) {
				entity.hurt(DamageSource.DROWN, 2.0F);
			}
		}
	}

	private void tick(IGamePhase game) {
		GamePhase currentPhase = phases.get();
		int prevWaterLevel = phaseToTideHeight.get(lastPhase.key);

		tickWaterLevel(game, currentPhase, prevWaterLevel);

		if (phasesIcebergsGrow.contains(currentPhase.key) && game.ticks() % icebergGrowthTickRate == 0) {
			growIcebergs(game.getWorld());
		}

		processRisingTideQueue(game);

		if (game.ticks() % 10 == 0) {
			spawnRisingTideParticles(game);
		}
	}

	private void spawnRisingTideParticles(IGamePhase game) {
		ServerWorld world = game.getWorld();
		Random random = world.random;

		BlockPos.Mutable mutablePos = new BlockPos.Mutable();

		for (ServerPlayerEntity player : game.getParticipants()) {
			// only attempt to spawn particles if the player is near the water surface
			if (Math.abs(player.getY() - waterLevel) > 10) {
				continue;
			}

			int particleX = MathHelper.floor(player.getX()) - random.nextInt(10) + random.nextInt(10);
			int particleZ = MathHelper.floor(player.getZ()) - random.nextInt(10) + random.nextInt(10);
			mutablePos.set(particleX, waterLevel, particleZ);

			if (!world.isEmptyBlock(mutablePos) && world.isEmptyBlock(mutablePos.move(Direction.UP))) {
				IPacket<?> packet = new SSpawnParticlePacket(ParticleTypes.SPLASH, true, particleX, waterLevel + 1, particleZ, 0.1F, 0.0F, 0.1F, 0.0F, 4);
				player.connection.send(packet);
			}
		}
	}

	private void growIcebergs(final World world) {
		for (IcebergLine line : icebergLines) {
			line.generate(world, waterLevel);
		}
	}

	private int calculateWaterChangeInterval(int targetLevel, int prevLevel, int phaseLength) {
		int waterLevelDiff = prevLevel - targetLevel;
		return Math.max(phaseLength / Math.max(1, Math.abs(waterLevelDiff)), 1);
	}

	private void processRisingTideQueue(IGamePhase game) {
		if (highPriorityUpdates.isEmpty() && lowPriorityUpdates.isEmpty()) {
			return;
		}

		int count = this.processUpdates(game, highPriorityUpdates.iterator(), HIGH_PRIORITY_BUDGET_PER_TICK);
		if (count <= 0) {
			this.processUpdates(game, lowPriorityUpdates.iterator(), LOW_PRIORITY_BUDGET_PER_TICK);
		}
	}

	private int processUpdates(IGamePhase game, LongIterator iterator, int maxToProcess) {
		ServerWorld world = game.getWorld();
		ServerChunkProvider chunkProvider = world.getChunkSource();

		long startTime = System.currentTimeMillis();
		long updatedBlocks = 0;

		int count = 0;

		while (count < maxToProcess && iterator.hasNext()) {
			long chunkPos = iterator.nextLong();
			int chunkX = ChunkPos.getX(chunkPos);
			int chunkZ = ChunkPos.getZ(chunkPos);

			// we only want to apply updates to loaded chunks
			Chunk chunk = chunkProvider.getChunkNow(chunkX, chunkZ);
			if (chunk == null) {
				continue;
			}

			iterator.remove();
			updatedBlocks += increaseTideForChunk(chunk);
			count++;
		}

		if (updatedBlocks > 0) {
			long endTime = System.currentTimeMillis();
			LogManager.getLogger().debug("Updated {} blocks in {}ms", updatedBlocks, endTime - startTime);
		}

		return count;
	}

	private void tickWaterLevel(final IGamePhase game, final GamePhase phase, final int prevWaterLevel) {
		final int targetWaterLevel = phaseToTideHeight.get(phase.key);

		int waterChangeInterval = this.calculateWaterChangeInterval(
				targetWaterLevel,
				prevWaterLevel,
				phase.lengthInTicks
		);

		if (waterLevel < targetWaterLevel && game.ticks() % waterChangeInterval == 0) {
			waterLevel++;

			long[] chunks = collectSortedChunks(game);

			boolean close = true;
			for (long chunkPos : chunks) {
				if (close) {
					highPriorityUpdates.add(chunkPos);
					int distance2 = getChunkDistance2(game, ChunkPos.getX(chunkPos), ChunkPos.getZ(chunkPos));
					if (distance2 >= HIGH_PRIORITY_DISTANCE_2) {
						close = false;
					}
				} else {
					lowPriorityUpdates.add(chunkPos);
				}
			}
		}
	}

	private long[] collectSortedChunks(IGamePhase game) {
		LongComparator distanceComparator = (pos1, pos2) -> Integer.compare(
				getChunkDistance2(game, ChunkPos.getX(pos1), ChunkPos.getZ(pos1)),
				getChunkDistance2(game, ChunkPos.getX(pos2), ChunkPos.getZ(pos2))
		);

		int sizeX = maxTideChunk.x - minTideChunk.x + 1;
		int sizeZ = maxTideChunk.z - minTideChunk.z + 1;
		long[] chunks = new long[sizeX * sizeZ];
		int length = 0;

		for (int z = minTideChunk.z; z <= maxTideChunk.z; z++) {
			for (int x = minTideChunk.x; x <= maxTideChunk.x; x++) {
				long chunkPos = ChunkPos.asLong(x, z);

				// insertion sort time!
				int index = LongArrays.binarySearch(chunks, 0, length, chunkPos, distanceComparator);
				if (index < 0) {
					index = -index - 1;
				}

				System.arraycopy(chunks, index, chunks, index + 1, length - index);

				chunks[index] = chunkPos;
				length++;
			}
		}

		return chunks;
	}

	private int getChunkDistance2(IGamePhase game, int x, int z) {
		int minDistance2 = Integer.MAX_VALUE;
		int centerX = (x << 4) + 8;
		int centerZ = (z << 4) + 8;
		for (ServerPlayerEntity player : game.getAllPlayers()) {
			int dx = MathHelper.floor(player.getX()) - centerX;
			int dz = MathHelper.floor(player.getZ()) - centerZ;
			int distance2 = dx * dx + dz * dz;
			if (distance2 < minDistance2) {
				minDistance2 = distance2;
			}
		}
		return minDistance2;
	}

	// thicc boi
	private long increaseTideForChunk(Chunk chunk) {
		ChunkPos chunkPos = chunk.getPos();

		int targetLevel = this.waterLevel;
		int lastLevel = this.chunkWaterLevels.put(chunkPos.toLong(), targetLevel);

		if (targetLevel > lastLevel) {
			return increaseTideForChunk(chunk, lastLevel, targetLevel);
		} else {
			return 0;
		}
	}

	private long increaseTideForChunk(Chunk chunk, int fromWaterLevel, int toWaterLevel) {
		World world = chunk.getLevel();
		ServerChunkProvider chunkProvider = (ServerChunkProvider) world.getChunkSource();

		ChunkPos chunkPos = chunk.getPos();

		Heightmap heightmapSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Type.WORLD_SURFACE);
		Heightmap heightmapMotionBlocking = chunk.getOrCreateHeightmapUnprimed(Heightmap.Type.MOTION_BLOCKING);

		int fromY = fromWaterLevel;
		int toY = toWaterLevel;

		// this is the total area over which we need to increase the tide
		BlockPos chunkMin = new BlockPos(
				Math.max(tideArea.min.getX(), chunkPos.getMinBlockX()),
				fromY,
				Math.max(tideArea.min.getZ(), chunkPos.getMinBlockZ())
		);
		BlockPos chunkMax = new BlockPos(
				Math.min(tideArea.max.getX(), chunkPos.getMaxBlockX()),
				toY,
				Math.min(tideArea.max.getZ(), chunkPos.getMaxBlockZ())
		);

		long updatedBlocks = 0;

		int fromSection = fromY >> 4;
		int toSection = toY >> 4;

		// iterate through all the sections that need to be changed
		for (int sectionY = fromSection; sectionY <= toSection; sectionY++) {
			ChunkSection section = getOrCreateSection(chunk, sectionY);
			int minSectionY = sectionY << 4;
			int maxSectionY = minSectionY + 15;

			// Calculate start/end within the current section
			BlockPos sectionMin = new BlockPos(chunkMin.getX(), Math.max(chunkMin.getY(), minSectionY), chunkMin.getZ());
			BlockPos sectionMax = new BlockPos(chunkMax.getX(), Math.min(chunkMax.getY(), maxSectionY), chunkMax.getZ());

			for (BlockPos worldPos : BlockPos.betweenClosed(sectionMin, sectionMax)) {
				int localX = worldPos.getX() & 15;
				int localY = worldPos.getY() & 15;
				int localZ = worldPos.getZ() & 15;

				BlockState existingBlock = section.getBlockState(localX, localY, localZ);

				BlockState newBlock = mapBlock(existingBlock, worldPos.getY(), fromWaterLevel);
				if (newBlock == null) continue;

				if (existingBlock.getBlock() != Blocks.BAMBOO) {
					section.setBlockState(localX, localY, localZ, newBlock);
				} else {
					world.setBlock(worldPos, newBlock, Constants.BlockFlags.NO_RERENDER | Constants.BlockFlags.BLOCK_UPDATE);
				}

				// Update heightmap
				heightmapSurface.update(localX, localY, localZ, newBlock);
				heightmapMotionBlocking.update(localX, localY, localZ, newBlock);

				// Tell the client about the change
				chunkProvider.blockChanged(worldPos);
				// FIXES LIGHTING AT THE COST OF PERFORMANCE - TODO ask fry?
				// chunkProvider.getLightManager().checkBlock(realPos);

				updatedBlocks++;
			}
		}

		if (updatedBlocks > 0) {
			// Make sure this chunk gets saved
			chunk.markUnsaved();
		}

		return updatedBlocks;
	}

	private static ChunkSection getOrCreateSection(Chunk chunk, int sectionY) {
		ChunkSection section = chunk.getSections()[sectionY];
		if (section == Chunk.EMPTY_SECTION) {
			section = new ChunkSection(sectionY << 4);
			chunk.getSections()[sectionY] = section;
		}

		return section;
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

		if (state.isAir() || !state.getMaterial().blocksMotion() || block == Blocks.BAMBOO || block.getRegistryName().toString().equals("weather2:sand_layer")) {
			// If air or a replaceable block, just set to water
			return Blocks.WATER.defaultBlockState();
		}

		if (block instanceof IWaterLoggable) {
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

	@Nullable
	private static BlockState mapBlockBelowWater(BlockState state) {
		if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.GRASS_PATH) {
			return Blocks.DIRT.defaultBlockState();
		}

		return null;
	}
}
