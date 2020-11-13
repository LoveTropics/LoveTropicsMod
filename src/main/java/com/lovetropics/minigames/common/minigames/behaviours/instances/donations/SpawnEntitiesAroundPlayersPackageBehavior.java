package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.game_actions.GamePackage;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.*;

public class SpawnEntitiesAroundPlayersPackageBehavior implements IMinigameBehavior
{
	private final DonationPackageData data;
	private final ResourceLocation entityId;
	private final int entityCountPerPlayer;
	private final int spawnDistanceMin;
	private final int spawnDistanceMax;
	private final int spawnRangeY;
	private final int spawnsPerTick;
	private HashMap<ServerPlayerEntity, Integer> playerToAmountToSpawn = new HashMap<>();

	public SpawnEntitiesAroundPlayersPackageBehavior(final DonationPackageData data, final ResourceLocation entityId, final int entityCount, final int spawnDistanceMin, final int spawnDistanceMax, final int spawnRangeY, final int spawnsPerTick) {
		this.data = data;
		this.entityId = entityId;
		this.entityCountPerPlayer = entityCount;
		this.spawnDistanceMin = spawnDistanceMin;
		this.spawnDistanceMax = spawnDistanceMax;
		this.spawnRangeY = spawnRangeY;
		this.spawnsPerTick = spawnsPerTick;
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {

	}

	public static <T> SpawnEntitiesAroundPlayersPackageBehavior parse(Dynamic<T> root) {
		final DonationPackageData data = DonationPackageData.parse(root);
		final ResourceLocation entityId = new ResourceLocation(root.get("entity_id").asString(""));
		final int entityCountPerPlayer = root.get("entity_count_per_player").asInt(1);
		final int spawnDistanceMin = root.get("spawn_distance_min").asInt(10);
		final int spawnDistanceMax = root.get("spawn_distance_max").asInt(20);
		final int spawnRangeY = root.get("spawn_range_y").asInt(10);
		final int spawnsPerTick = root.get("spawn_try_rate").asInt(10);

		return new SpawnEntitiesAroundPlayersPackageBehavior(data, entityId, entityCountPerPlayer, spawnDistanceMin, spawnDistanceMax, spawnRangeY, spawnsPerTick);
	}

	@Override
	public boolean onGamePackageReceived(final IMinigameInstance minigame, final GamePackage gamePackage) {
		if (gamePackage.getPackageType().equals(data.packageType)) {
			final List<ServerPlayerEntity> players = Lists.newArrayList(minigame.getParticipants());
			for (ServerPlayerEntity player : players) {
				playerToAmountToSpawn.put(player, entityCountPerPlayer);
			}

			minigame.getParticipants().forEach(player -> data.onReceive(player, gamePackage.getSendingPlayerName()));

			return true;
		}

		return false;
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
		Iterator<Map.Entry<ServerPlayerEntity, Integer>> it = playerToAmountToSpawn.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<ServerPlayerEntity, Integer> entry = it.next();

			if (!entry.getKey().isAlive()) {
				it.remove();
			} else {

				BlockPos pos = getSpawnableRandomPositionNear(minigame, entry.getKey().getPosition(), spawnDistanceMin, spawnDistanceMax, spawnsPerTick, spawnRangeY);

				if (pos != BlockPos.ZERO) {
					entry.setValue(entry.getValue() - 1);
					if (entry.getValue() <= 0) {
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
	 * @param minigame
	 * @param pos
	 * @param minDist
	 * @param maxDist
	 * @param loopAttempts
	 * @param yRange
	 * @return BlockPos.ZERO if it fails, otherwise a real position
	 */
	public BlockPos getSpawnableRandomPositionNear(final IMinigameInstance minigame, BlockPos pos, int minDist, int maxDist, int loopAttempts, int yRange) {
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
	 *
	 * @param minigame
	 * @param pos
	 * @return
	 */
	public boolean isSpawnablePosition(final IMinigameInstance minigame, BlockPos pos) {
		if (minigame.getWorld().isAirBlock(pos.add(0, -1, 0))) return false;
		if (!minigame.getWorld().isAirBlock(pos.add(0, 0, 0))) return false;
		if (!minigame.getWorld().isAirBlock(pos.add(0, 1, 0))) return false;
		if (minigame.getWorld().getBlockState(pos.add(0, -1, 0)).getMaterial().isLiquid()) return false;
		if (minigame.getWorld().getBlockState(pos.add(0, 0, 0)).getMaterial().isLiquid()) return false;
		if (minigame.getWorld().getBlockState(pos.add(0, 1, 0)).getMaterial().isLiquid()) return false;
		return true;
	}


}
