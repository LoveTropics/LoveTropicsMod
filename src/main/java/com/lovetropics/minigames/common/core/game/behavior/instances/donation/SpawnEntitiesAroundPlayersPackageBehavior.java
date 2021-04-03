package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.Iterator;

public class SpawnEntitiesAroundPlayersPackageBehavior implements IGameBehavior
{
	public static final Codec<SpawnEntitiesAroundPlayersPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Registry.ENTITY_TYPE.fieldOf("entity_id").forGetter(c -> c.entityId),
				Codec.INT.optionalFieldOf("entity_count_per_player", 1).forGetter(c -> c.entityCountPerPlayer),
				Codec.INT.optionalFieldOf("spawn_distance_min", 10).forGetter(c -> c.spawnDistanceMin),
				Codec.INT.optionalFieldOf("spawn_distance_max", 20).forGetter(c -> c.spawnDistanceMax),
				Codec.INT.optionalFieldOf("spawn_range_y", 10).forGetter(c -> c.spawnRangeY),
				Codec.INT.optionalFieldOf("spawn_try_rate", 10).forGetter(c -> c.spawnsPerTick)
		).apply(instance, SpawnEntitiesAroundPlayersPackageBehavior::new);
	});

	private final EntityType<?> entityId;
	private final int entityCountPerPlayer;
	private final int spawnDistanceMin;
	private final int spawnDistanceMax;
	private final int spawnRangeY;
	private final int spawnsPerTick;
	private final Object2IntMap<ServerPlayerEntity> playerToAmountToSpawn = new Object2IntOpenHashMap<>();

	public SpawnEntitiesAroundPlayersPackageBehavior(final EntityType<?> entityId, final int entityCount, final int spawnDistanceMin, final int spawnDistanceMax, final int spawnRangeY, final int spawnsPerTick) {
		this.entityId = entityId;
		this.entityCountPerPlayer = entityCount;
		this.spawnDistanceMin = spawnDistanceMin;
		this.spawnDistanceMax = spawnDistanceMax;
		this.spawnRangeY = spawnRangeY;
		this.spawnsPerTick = spawnsPerTick;
	}

	@Override
	public void register(IActiveGame registerGame, EventRegistrar events) {
		events.listen(GamePackageEvents.APPLY_PACKAGE, (game, player, sendingPlayer) -> playerToAmountToSpawn.put(player, entityCountPerPlayer));
		events.listen(GameLifecycleEvents.TICK, this::tick);
	}

	private void tick(IActiveGame game) {
		Iterator<Object2IntMap.Entry<ServerPlayerEntity>> it = playerToAmountToSpawn.object2IntEntrySet().iterator();
		while (it.hasNext()) {
			Object2IntMap.Entry<ServerPlayerEntity> entry = it.next();

			if (!entry.getKey().isAlive()) {
				it.remove();
			} else {

				BlockPos pos = getSpawnableRandomPositionNear(game, entry.getKey().getPosition(), spawnDistanceMin, spawnDistanceMax, spawnsPerTick, spawnRangeY);

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
	public BlockPos getSpawnableRandomPositionNear(final IActiveGame minigame, BlockPos pos, int minDist, int maxDist, int loopAttempts, int yRange) {
		for (int i = 0; i < loopAttempts; i++) {
			BlockPos posTry = pos.add(minigame.getWorld().getRandom().nextInt(maxDist * 2) - maxDist,
					minigame.getWorld().getRandom().nextInt(yRange * 2) - yRange,
					minigame.getWorld().getRandom().nextInt(maxDist * 2) - maxDist);

			if (pos.distanceSq(posTry) >= minDist * minDist && isSpawnablePosition(minigame, posTry)) {
				return posTry;
			}
		}
		return BlockPos.ZERO;
	}

	/**
	 * Quick and dirty check for 2 high air with non air block under it
	 * - also checks that it isnt water under it
	 */
	public boolean isSpawnablePosition(final IActiveGame minigame, BlockPos pos) {
		return !minigame.getWorld().isAirBlock(pos.add(0, -1, 0))
				&& minigame.getWorld().isAirBlock(pos.add(0, 0, 0))
				&& minigame.getWorld().isAirBlock(pos.add(0, 1, 0))
				&& !minigame.getWorld().getBlockState(pos.add(0, -1, 0)).getMaterial().isLiquid()
				&& !minigame.getWorld().getBlockState(pos.add(0, 0, 0)).getMaterial().isLiquid()
				&& !minigame.getWorld().getBlockState(pos.add(0, 1, 0)).getMaterial().isLiquid();
	}


}
