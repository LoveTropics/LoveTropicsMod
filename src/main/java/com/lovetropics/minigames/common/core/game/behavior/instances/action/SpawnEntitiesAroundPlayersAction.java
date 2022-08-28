package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Iterator;

public class SpawnEntitiesAroundPlayersAction implements IGameBehavior
{
	public static final Codec<SpawnEntitiesAroundPlayersAction> CODEC = RecordCodecBuilder.create(i -> i.group(
			ForgeRegistries.ENTITIES.getCodec().fieldOf("entity_id").forGetter(c -> c.entityId),
			Codec.INT.optionalFieldOf("entity_count_per_player", 1).forGetter(c -> c.entityCountPerPlayer),
			Codec.INT.optionalFieldOf("spawn_distance_min", 10).forGetter(c -> c.spawnDistanceMin),
			Codec.INT.optionalFieldOf("spawn_distance_max", 20).forGetter(c -> c.spawnDistanceMax),
			Codec.INT.optionalFieldOf("spawn_range_y", 10).forGetter(c -> c.spawnRangeY),
			Codec.INT.optionalFieldOf("spawn_try_rate", 10).forGetter(c -> c.spawnsPerTick)
	).apply(i, SpawnEntitiesAroundPlayersAction::new));

	private final EntityType<?> entityId;
	private final int entityCountPerPlayer;
	private final int spawnDistanceMin;
	private final int spawnDistanceMax;
	private final int spawnRangeY;
	private final int spawnsPerTick;
	private final Object2IntMap<ServerPlayer> playerToAmountToSpawn = new Object2IntOpenHashMap<>();

	public SpawnEntitiesAroundPlayersAction(final EntityType<?> entityId, final int entityCount, final int spawnDistanceMin, final int spawnDistanceMax, final int spawnRangeY, final int spawnsPerTick) {
		this.entityId = entityId;
		this.entityCountPerPlayer = entityCount;
		this.spawnDistanceMin = spawnDistanceMin;
		this.spawnDistanceMax = spawnDistanceMax;
		this.spawnRangeY = spawnRangeY;
		this.spawnsPerTick = spawnsPerTick;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			playerToAmountToSpawn.put(player, entityCountPerPlayer);
			return true;
		});
		events.listen(GamePhaseEvents.TICK, () -> tick(game));
	}

	private void tick(IGamePhase game) {
		Iterator<Object2IntMap.Entry<ServerPlayer>> it = playerToAmountToSpawn.object2IntEntrySet().iterator();
		while (it.hasNext()) {
			Object2IntMap.Entry<ServerPlayer> entry = it.next();

			if (!entry.getKey().isAlive()) {
				it.remove();
			} else {

				BlockPos pos = getSpawnableRandomPositionNear(game, entry.getKey().blockPosition(), spawnDistanceMin, spawnDistanceMax, spawnsPerTick, spawnRangeY);

				if (pos != BlockPos.ZERO) {
					entry.setValue(entry.getIntValue() - 1);
					if (entry.getIntValue() <= 0) {
						it.remove();
					}
					Util.spawnEntity(entityId, game.getWorld(), pos.getX(), pos.getY(), pos.getZ());
				}

			}
		}
	}

	/**
	 * Tries to return a random spawnable position within the set distances up to a certain amount of attempts
	 *
	 * @return BlockPos.ZERO if it fails, otherwise a real position
	 */
	public BlockPos getSpawnableRandomPositionNear(final IGamePhase game, BlockPos pos, int minDist, int maxDist, int loopAttempts, int yRange) {
		for (int i = 0; i < loopAttempts; i++) {
			BlockPos posTry = pos.offset(game.getWorld().getRandom().nextInt(maxDist * 2) - maxDist,
					game.getWorld().getRandom().nextInt(yRange * 2) - yRange,
					game.getWorld().getRandom().nextInt(maxDist * 2) - maxDist);

			if (pos.distSqr(posTry) >= minDist * minDist && isSpawnablePosition(game, posTry)) {
				return posTry;
			}
		}
		return BlockPos.ZERO;
	}

	/**
	 * Quick and dirty check for 2 high air with non air block under it
	 * - also checks that it isnt water under it
	 */
	public boolean isSpawnablePosition(final IGamePhase game, BlockPos pos) {
		ServerLevel world = game.getWorld();
		return !world.isEmptyBlock(pos.offset(0, -1, 0))
				&& world.isEmptyBlock(pos.offset(0, 0, 0))
				&& world.isEmptyBlock(pos.offset(0, 1, 0))
				&& !world.getBlockState(pos.offset(0, -1, 0)).getMaterial().isLiquid()
				&& !world.getBlockState(pos.offset(0, 0, 0)).getMaterial().isLiquid()
				&& !world.getBlockState(pos.offset(0, 1, 0)).getMaterial().isLiquid();
	}


}
