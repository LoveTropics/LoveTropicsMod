package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.survive_the_tide.IcebergLine;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.GameProgressionState;
import com.lovetropics.minigames.common.core.game.state.ProgressionPeriod;
import com.lovetropics.minigames.common.core.game.state.ProgressionSpline;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RisingTidesGameBehavior implements IGameBehavior {
	public static final MapCodec<RisingTidesGameBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.optionalFieldOf("tide_area_region", "tide_area").forGetter(c -> c.tideAreaKey),
			Codec.STRING.optionalFieldOf("iceberg_lines_region", "iceberg_lines").forGetter(c -> c.icebergLinesKey),
			ProgressionSpline.CODEC.fieldOf("water_levels").forGetter(c -> c.waterLevels),
			ProgressionPeriod.CODEC.optionalFieldOf("iceberg_growth_period").forGetter(c -> c.icebergGrowthPeriod),
			Codec.INT.optionalFieldOf("iceberg_growth_steps", 0).forGetter(c -> c.maxIcebergGrowthSteps)
	).apply(i, RisingTidesGameBehavior::new));

	private static final RegistryObject<Block> WATER_BARRIER = RegistryObject.create(new ResourceLocation("ltextras", "water_barrier"), ForgeRegistries.BLOCKS);
	private static final RegistryObject<Block> SAND_LAYER = RegistryObject.create(new ResourceLocation("weather2", "sand_layer"), ForgeRegistries.BLOCKS);

	private static final int HIGH_PRIORITY_BUDGET_PER_TICK = 50;
	private static final int LOW_PRIORITY_BUDGET_PER_TICK = 10;

	private static final int HIGH_PRIORITY_DISTANCE_2 = 64 * 64;

	private final String tideAreaKey;
	private final String icebergLinesKey;
	private final ProgressionSpline waterLevels;
	private final Optional<ProgressionPeriod> icebergGrowthPeriod;
	private final int maxIcebergGrowthSteps;
	private int waterLevel;

	private BlockBox tideArea;
	private final List<IcebergLine> icebergLines = new ArrayList<>();
	private int icebergGrowthSteps;
	private Float2FloatFunction waterLevelByTime = time -> 0.0f;

	private ChunkPos minTideChunk;
	private ChunkPos maxTideChunk;

	private final LongSet highPriorityUpdates = new LongLinkedOpenHashSet();
	private final LongSet lowPriorityUpdates = new LongLinkedOpenHashSet();
	private final Long2IntMap chunkWaterLevels = new Long2IntOpenHashMap();

	private GameProgressionState progression;

	public RisingTidesGameBehavior(String tideAreaKey, String icebergLinesKey, final ProgressionSpline waterLevels, final Optional<ProgressionPeriod> icebergGrowthPeriod, final int maxIcebergGrowthSteps) {
		this.tideAreaKey = tideAreaKey;
		this.icebergLinesKey = icebergLinesKey;
		this.waterLevels = waterLevels;
		this.icebergGrowthPeriod = icebergGrowthPeriod;
		this.maxIcebergGrowthSteps = maxIcebergGrowthSteps;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		tideArea = game.getMapRegions().getOrThrow(tideAreaKey);

		minTideChunk = new ChunkPos(SectionPos.blockToSectionCoord(tideArea.min().getX()), SectionPos.blockToSectionCoord(tideArea.min().getZ()));
		maxTideChunk = new ChunkPos(SectionPos.blockToSectionCoord(tideArea.max().getX()), SectionPos.blockToSectionCoord(tideArea.max().getZ()));

		icebergLines.clear();

		ServerLevel level = game.getWorld();
		for (BlockBox icebergLine : game.getMapRegions().get(icebergLinesKey)) {
			int startX = icebergLine.min().getX();
			int startZ = icebergLine.min().getZ();
			int endX = icebergLine.max().getX();
			int endZ = icebergLine.max().getZ();

			icebergLines.add(new IcebergLine(
					new BlockPos(startX, level.getMinBuildHeight(), startZ),
					new BlockPos(endX, level.getMinBuildHeight(), endZ),
					10
			));
			icebergLines.add(new IcebergLine(
					new BlockPos(endX, level.getMinBuildHeight(), startZ),
					new BlockPos(startX, level.getMinBuildHeight(), endZ),
					10
			));
		}

		progression = game.getState().getOrThrow(GameProgressionState.KEY);

		waterLevelByTime = waterLevels.resolve(progression);

		events.listen(GamePhaseEvents.START, () -> {
			waterLevel = Mth.floor(waterLevelByTime.get(progression.time()));
			chunkWaterLevels.defaultReturnValue(waterLevel);
		});

		events.listen(GameLivingEntityEvents.TICK, this::onLivingEntityUpdate);
		events.listen(GamePhaseEvents.TICK, () -> tick(game));
	}

	private void onLivingEntityUpdate(LivingEntity entity) {
		// NOTE: DO NOT REMOVE THIS CHECK, CAUSES FISH TO DIE AND SPAWN ITEMS ON DEATH
		// FISH WILL KEEP SPAWNING, DYING AND COMPLETELY SLOW THE SERVER TO A CRAWL
		if (!entity.canBreatheUnderwater()) {
			if (entity.getY() <= this.waterLevel + 1 && entity.isInWater() && entity.tickCount % 40 == 0) {
				entity.hurt(entity.damageSources().drown(), 2.0F);
			}
		}
	}

	private void tick(IGamePhase game) {
		tickWaterLevel(game);

		icebergGrowthPeriod.ifPresent(period -> {
			float icebergsProgress = progression.progressIn(period);
			if (icebergsProgress > 0.0f) {
				int targetSteps = Math.round(icebergsProgress * maxIcebergGrowthSteps);
				if (icebergGrowthSteps < targetSteps) {
					growIcebergs(game.getWorld());
					icebergGrowthSteps++;
				}
			}
		});

		processRisingTideQueue(game);

		spawnRisingTideParticles(game);
	}

	private void spawnRisingTideParticles(IGamePhase game) {
		ServerLevel world = game.getWorld();
		RandomSource random = world.random;
		if (random.nextInt(3) != 0) {
			return;
		}

		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

		for (ServerPlayer player : game.getParticipants()) {
			// only attempt to spawn particles if the player is near the water surface
			if (Math.abs(player.getY() - waterLevel) > 5) {
				continue;
			}

			int particleX = Mth.floor(player.getX()) - random.nextInt(5) + random.nextInt(5);
			int particleZ = Mth.floor(player.getZ()) - random.nextInt(5) + random.nextInt(5);
			mutablePos.set(particleX, waterLevel, particleZ);

			if (!world.isEmptyBlock(mutablePos) && world.isEmptyBlock(mutablePos.move(Direction.UP))) {
				Packet<?> packet = new ClientboundLevelParticlesPacket(ParticleTypes.SPLASH, false, particleX, waterLevel + 1, particleZ, 0.1F, 0.0F, 0.1F, 0.0F, 4);
				player.connection.send(packet);
			}
		}
	}

	private void growIcebergs(final Level world) {
		for (IcebergLine line : icebergLines) {
			line.generate(world, waterLevel);
		}
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
		ServerLevel world = game.getWorld();
		ServerChunkCache chunkProvider = world.getChunkSource();

		int count = 0;

		while (count < maxToProcess && iterator.hasNext()) {
			long chunkPos = iterator.nextLong();
			int chunkX = ChunkPos.getX(chunkPos);
			int chunkZ = ChunkPos.getZ(chunkPos);

			// we only want to apply updates to loaded chunks
			LevelChunk chunk = chunkProvider.getChunkNow(chunkX, chunkZ);
			if (chunk == null) {
				continue;
			}

			iterator.remove();
			increaseTideForChunk(chunk);
			count++;
		}

		return count;
	}

	private void tickWaterLevel(final IGamePhase game) {
		int targetWaterLevel = Mth.floor(waterLevelByTime.get(progression.time()));

		if (waterLevel < targetWaterLevel) {
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
		int centerX = SectionPos.sectionToBlockCoord(x) + SectionPos.SECTION_HALF_SIZE;
		int centerZ = SectionPos.sectionToBlockCoord(z) + SectionPos.SECTION_HALF_SIZE;
		for (ServerPlayer player : game.getAllPlayers()) {
			int dx = Mth.floor(player.getX()) - centerX;
			int dz = Mth.floor(player.getZ()) - centerZ;
			int distance2 = dx * dx + dz * dz;
			if (distance2 < minDistance2) {
				minDistance2 = distance2;
			}
		}
		return minDistance2;
	}

	// thicc boi
	private long increaseTideForChunk(LevelChunk chunk) {
		ChunkPos chunkPos = chunk.getPos();

		int targetLevel = this.waterLevel;
		int lastLevel = this.chunkWaterLevels.put(chunkPos.toLong(), targetLevel);

		if (targetLevel > lastLevel) {
			return increaseTideForChunk(chunk, lastLevel, targetLevel);
		} else {
			return 0;
		}
	}

	private long increaseTideForChunk(LevelChunk chunk, int fromWaterLevel, int toWaterLevel) {
		Level world = chunk.getLevel();
		ServerChunkCache chunkProvider = (ServerChunkCache) world.getChunkSource();

		ChunkPos chunkPos = chunk.getPos();

		Heightmap heightmapSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE);
		Heightmap heightmapMotionBlocking = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.MOTION_BLOCKING);

		int fromY = fromWaterLevel;
		int toY = toWaterLevel;

		// this is the total area over which we need to increase the tide
		BlockPos chunkMin = new BlockPos(
				Math.max(tideArea.min().getX(), chunkPos.getMinBlockX()),
				fromY,
				Math.max(tideArea.min().getZ(), chunkPos.getMinBlockZ())
		);
		BlockPos chunkMax = new BlockPos(
				Math.min(tideArea.max().getX(), chunkPos.getMaxBlockX()),
				toY,
				Math.min(tideArea.max().getZ(), chunkPos.getMaxBlockZ())
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

				BlockState newBlock = mapBlock(existingBlock, worldPos.getY(), fromWaterLevel);
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
