package com.lovetropics.minigames.common.content.survive_the_tide.behavior;

import com.lovetropics.lib.BlockBox;
import com.lovetropics.minigames.common.content.survive_the_tide.TideFiller;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLivingEntityEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.progress.ProgressChannel;
import com.lovetropics.minigames.common.core.game.state.progress.ProgressHolder;
import com.lovetropics.minigames.common.core.game.state.progress.ProgressionSpline;
import com.lovetropics.minigames.common.core.network.RiseTideMessage;
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
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;

public class RisingTidesGameBehavior implements IGameBehavior {
	public static final MapCodec<RisingTidesGameBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.STRING.optionalFieldOf("tide_area_region", "tide_area").forGetter(c -> c.tideAreaKey),
			ProgressionSpline.CODEC.fieldOf("water_levels").forGetter(c -> c.waterLevels)
	).apply(i, RisingTidesGameBehavior::new));

	private static final int HIGH_PRIORITY_BUDGET_PER_TICK = 40;
	private static final int LOW_PRIORITY_BUDGET_PER_TICK = 8;

	private static final int HIGH_PRIORITY_DISTANCE_2 = 64 * 64;

	private final String tideAreaKey;
	private final ProgressionSpline waterLevels;
	private int waterLevel;

	private BlockBox tideArea;
	private Float2FloatFunction waterLevelByTime = time -> 0.0f;

	private ChunkPos minTideChunk;
	private ChunkPos maxTideChunk;

	private final LongSet highPriorityUpdates = new LongLinkedOpenHashSet();
	private final LongSet lowPriorityUpdates = new LongLinkedOpenHashSet();
	private final Long2IntMap chunkWaterLevels = new Long2IntOpenHashMap();

	private ProgressHolder progression;

	public RisingTidesGameBehavior(String tideAreaKey, final ProgressionSpline waterLevels) {
		this.tideAreaKey = tideAreaKey;
		this.waterLevels = waterLevels;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		tideArea = game.mapRegions().getOrThrow(tideAreaKey);

		minTideChunk = new ChunkPos(SectionPos.blockToSectionCoord(tideArea.min().getX()), SectionPos.blockToSectionCoord(tideArea.min().getZ()));
		maxTideChunk = new ChunkPos(SectionPos.blockToSectionCoord(tideArea.max().getX()), SectionPos.blockToSectionCoord(tideArea.max().getZ()));

		progression = ProgressChannel.MAIN.getOrThrow(game);

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
			if (entity.getY() <= waterLevel + 1 && entity.isInWater() && entity.tickCount % 40 == 0) {
				entity.hurt(entity.damageSources().drown(), 2.0F);
			}
		}
	}

	private void tick(IGamePhase game) {
		tickWaterLevel(game);
		processRisingTideQueue(game);

		spawnRisingTideParticles(game);
	}

	private void spawnRisingTideParticles(IGamePhase game) {
		ServerLevel world = game.level();
		RandomSource random = world.random;
		if (random.nextInt(3) != 0) {
			return;
		}

		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

		for (ServerPlayer player : game.participants()) {
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

	private void processRisingTideQueue(IGamePhase game) {
		if (highPriorityUpdates.isEmpty() && lowPriorityUpdates.isEmpty()) {
			return;
		}

		int count = processUpdates(game, highPriorityUpdates.iterator(), HIGH_PRIORITY_BUDGET_PER_TICK);
		if (count <= 0) {
			processUpdates(game, lowPriorityUpdates.iterator(), LOW_PRIORITY_BUDGET_PER_TICK);
		}
	}

	private int processUpdates(IGamePhase game, LongIterator iterator, int maxToProcess) {
		ServerLevel world = game.level();
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
			increaseTideForChunk(world, chunk);
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
		for (ServerPlayer player : game.allPlayers()) {
			int dx = Mth.floor(player.getX()) - centerX;
			int dz = Mth.floor(player.getZ()) - centerZ;
			int distance2 = dx * dx + dz * dz;
			if (distance2 < minDistance2) {
				minDistance2 = distance2;
			}
		}
		return minDistance2;
	}

	private long increaseTideForChunk(ServerLevel level, LevelChunk chunk) {
		ChunkPos chunkPos = chunk.getPos();

		int targetLevel = waterLevel;
		int lastLevel = chunkWaterLevels.put(chunkPos.toLong(), targetLevel);

		if (targetLevel > lastLevel) {
			BlockPos tideMin = tideArea.min();
			BlockPos tideMax = tideArea.max();
			long count = TideFiller.fillChunk(tideMin.getX(), tideMin.getZ(), tideMax.getX(), tideMax.getZ(), chunk, lastLevel, targetLevel);
			if (count > 0) {
				PacketDistributor.sendToPlayersTrackingChunk(level, chunk.getPos(), new RiseTideMessage(
						new BlockPos(Math.max(tideMin.getX(), chunkPos.getMinBlockX()), lastLevel, Math.max(tideMin.getZ(), chunkPos.getMinBlockZ())),
						new BlockPos(Math.min(tideMax.getX(), chunkPos.getMaxBlockX()), targetLevel, Math.min(tideMax.getZ(), chunkPos.getMaxBlockZ()))
				));
			}
			return count;
		} else {
			return 0;
		}
	}
}
