package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.util.Util;
import com.lovetropics.minigames.common.core.integration.game_actions.GamePackage;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGamePackageBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.server.ServerWorld;

import java.util.Iterator;
import java.util.List;

public class SpawnEntitiesAroundPlayersPackageBehavior implements IGamePackageBehavior
{
	public static final Codec<SpawnEntitiesAroundPlayersPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DonationPackageData.CODEC.forGetter(c -> c.data),
				Registry.ENTITY_TYPE.fieldOf("entity_id").forGetter(c -> c.entityId),
				Codec.INT.optionalFieldOf("entity_count_per_player", 1).forGetter(c -> c.entityCountPerPlayer),
				Codec.INT.optionalFieldOf("spawn_distance_min", 10).forGetter(c -> c.spawnDistanceMin),
				Codec.INT.optionalFieldOf("spawn_distance_max", 20).forGetter(c -> c.spawnDistanceMax),
				Codec.INT.optionalFieldOf("spawn_range_y", 10).forGetter(c -> c.spawnRangeY),
				Codec.INT.optionalFieldOf("spawn_try_rate", 10).forGetter(c -> c.spawnsPerTick)
		).apply(instance, SpawnEntitiesAroundPlayersPackageBehavior::new);
	});

	private final DonationPackageData data;
	private final EntityType<?> entityId;
	private final int entityCountPerPlayer;
	private final int spawnDistanceMin;
	private final int spawnDistanceMax;
	private final int spawnRangeY;
	private final int spawnsPerTick;
	private final Object2IntMap<ServerPlayerEntity> playerToAmountToSpawn = new Object2IntOpenHashMap<>();

	public SpawnEntitiesAroundPlayersPackageBehavior(final DonationPackageData data, final EntityType<?> entityId, final int entityCount, final int spawnDistanceMin, final int spawnDistanceMax, final int spawnRangeY, final int spawnsPerTick) {
		this.data = data;
		this.entityId = entityId;
		this.entityCountPerPlayer = entityCount;
		this.spawnDistanceMin = spawnDistanceMin;
		this.spawnDistanceMax = spawnDistanceMax;
		this.spawnRangeY = spawnRangeY;
		this.spawnsPerTick = spawnsPerTick;
	}

	@Override
	public String getPackageType() {
		return data.getPackageType();
	}

	@Override
	public boolean onGamePackageReceived(final IGameInstance minigame, final GamePackage gamePackage) {
		if (gamePackage.getPackageType().equals(data.packageType)) {
			final List<ServerPlayerEntity> players = Lists.newArrayList(minigame.getParticipants());
			for (ServerPlayerEntity player : players) {
				playerToAmountToSpawn.put(player, entityCountPerPlayer);
			}

			data.onReceive(minigame, null, gamePackage.getSendingPlayerName());

			return true;
		}

		return false;
	}

	@Override
	public void worldUpdate(IGameInstance minigame, ServerWorld world) {
		Iterator<Object2IntMap.Entry<ServerPlayerEntity>> it = playerToAmountToSpawn.object2IntEntrySet().iterator();
		while (it.hasNext()) {
			Object2IntMap.Entry<ServerPlayerEntity> entry = it.next();

			if (!entry.getKey().isAlive()) {
				it.remove();
			} else {

				BlockPos pos = getSpawnableRandomPositionNear(minigame, entry.getKey().getPosition(), spawnDistanceMin, spawnDistanceMax, spawnsPerTick, spawnRangeY);

				if (pos != BlockPos.ZERO) {
					entry.setValue(entry.getIntValue() - 1);
					if (entry.getIntValue() <= 0) {
						it.remove();
					}
					Util.spawnEntity(entityId, minigame.getWorld(), pos.getX(), pos.getY(), pos.getZ());
				}

			}
		}
	}

	/**
	 * Tries to return a random spawnable position within the set distances up to a certain amount of attempts
	 *
	 * @return BlockPos.ZERO if it fails, otherwise a real position
	 */
	public BlockPos getSpawnableRandomPositionNear(final IGameInstance minigame, BlockPos pos, int minDist, int maxDist, int loopAttempts, int yRange) {
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
	public boolean isSpawnablePosition(final IGameInstance minigame, BlockPos pos) {
		if (minigame.getWorld().isAirBlock(pos.add(0, -1, 0))) return false;
		if (!minigame.getWorld().isAirBlock(pos.add(0, 0, 0))) return false;
		if (!minigame.getWorld().isAirBlock(pos.add(0, 1, 0))) return false;
		if (minigame.getWorld().getBlockState(pos.add(0, -1, 0)).getMaterial().isLiquid()) return false;
		if (minigame.getWorld().getBlockState(pos.add(0, 0, 0)).getMaterial().isLiquid()) return false;
		if (minigame.getWorld().getBlockState(pos.add(0, 1, 0)).getMaterial().isLiquid()) return false;
		return true;
	}


}