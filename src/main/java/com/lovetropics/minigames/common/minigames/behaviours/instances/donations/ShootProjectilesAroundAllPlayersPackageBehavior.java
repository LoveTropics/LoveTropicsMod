package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.game_actions.GamePackage;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;

public class ShootProjectilesAroundAllPlayersPackageBehavior implements IMinigameBehavior
{
	private final DonationPackageData data;
	//private final ResourceLocation entityId;
	private final int entityCountPerPlayer;
	private final int spawnDistanceMax;
	private final int targetRandomness;
	private final int spawnRangeY;
	private final int spawnRateBase;
	private final int spawnRateRandom;
	private final int explosionStrength;
	private HashMap<ServerPlayerEntity, Integer> playerToAmountToSpawn = new HashMap<>();
	private HashMap<ServerPlayerEntity, Integer> playerToDelayToSpawn = new HashMap<>();

	public ShootProjectilesAroundAllPlayersPackageBehavior(final DonationPackageData data, /*final ResourceLocation entityId, */final int entityCount, final int spawnDistanceMax, final int spawnRangeY, final int spawnsPerTickBase, final int spawnsPerTickRandom, final int targetRandomness, final int explosionStrength) {
		this.data = data;
		//this.entityId = entityId;
		this.entityCountPerPlayer = entityCount;
		this.spawnDistanceMax = spawnDistanceMax;
		this.targetRandomness = targetRandomness;
		this.spawnRangeY = spawnRangeY;
		this.spawnRateBase = spawnsPerTickBase;
		this.spawnRateRandom = spawnsPerTickRandom;
		this.explosionStrength = explosionStrength;
	}

	@Override
	public void onConstruct(IMinigameInstance minigame) {

	}

	public static <T> ShootProjectilesAroundAllPlayersPackageBehavior parse(Dynamic<T> root) {
		final DonationPackageData data = DonationPackageData.parse(root);
		//final ResourceLocation entityId = new ResourceLocation(root.get("entity_id").asString(""));
		final int entityCountPerPlayer = root.get("entity_count_per_player").asInt(10);
		final int spawnDistanceMax = root.get("spawn_distance_max").asInt(40);
		final int targetRandomness = root.get("target_randomness").asInt(10);
		final int spawnHeight = root.get("spawn_height").asInt(20);
		final int spawnRateBase = root.get("spawn_rate_base").asInt(20);
		final int spawnRateRandom = root.get("spawn_rate_random").asInt(20);
		final int explosionStrength = root.get("explosion_strength").asInt(2);

		return new ShootProjectilesAroundAllPlayersPackageBehavior(data, /*entityId, */entityCountPerPlayer, spawnDistanceMax, spawnHeight, spawnRateBase, spawnRateRandom, targetRandomness, explosionStrength);
	}

	@Override
	public boolean onGamePackageReceived(final IMinigameInstance minigame, final GamePackage gamePackage) {
		if (gamePackage.getPackageType().equals(data.packageType)) {
			final List<ServerPlayerEntity> players = Lists.newArrayList(minigame.getParticipants());
			for (ServerPlayerEntity player : players) {
				playerToAmountToSpawn.put(player, entityCountPerPlayer);
			}

			minigame.getParticipants().forEach(player -> data.onReceive(minigame, player, gamePackage.getSendingPlayerName()));

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
				playerToDelayToSpawn.remove(entry.getKey());
			} else {

				int cooldown = 0;
				if (playerToDelayToSpawn.containsKey(entry.getKey())) {
					cooldown = playerToDelayToSpawn.get(entry.getKey());
				}
				if (cooldown > 0) {
					cooldown--;
					playerToDelayToSpawn.put(entry.getKey(), cooldown);
				} else {
					cooldown = spawnRateBase + minigame.getWorld().getRandom().nextInt(spawnRateRandom);
					playerToDelayToSpawn.put(entry.getKey(), cooldown);

					BlockPos posSpawn = entry.getKey().getPosition().add(minigame.getWorld().getRandom().nextInt(spawnDistanceMax * 2) - spawnDistanceMax,20,
							minigame.getWorld().getRandom().nextInt(spawnDistanceMax * 2) - spawnDistanceMax);

					BlockPos posTarget = entry.getKey().getPosition().add(minigame.getWorld().getRandom().nextInt(targetRandomness * 2) - targetRandomness,0,
							minigame.getWorld().getRandom().nextInt(targetRandomness * 2) - targetRandomness);

					entry.setValue(entry.getValue() - 1);
					if (entry.getValue() <= 0) {
						it.remove();
						playerToDelayToSpawn.remove(entry.getKey());
					}
					double d2 = posTarget.getX() - (posSpawn.getX());
					double d3 = posTarget.getY() - (posSpawn.getY());
					double d4 = posTarget.getZ() - (posSpawn.getZ());
					FireballEntity fireballentity = new FireballEntity(EntityType.FIREBALL, world);
					fireballentity.setLocationAndAngles(posSpawn.getX(), posSpawn.getY(), posSpawn.getZ(), fireballentity.rotationYaw, fireballentity.rotationPitch);
					fireballentity.setPosition(posSpawn.getX(), posSpawn.getY(), posSpawn.getZ());
					double d0 = (double)MathHelper.sqrt(d2 * d2 + d3 * d3 + d4 * d4);
					fireballentity.accelerationX = d2 / d0 * 0.1D;
					fireballentity.accelerationY = d3 / d0 * 0.1D;
					fireballentity.accelerationZ = d4 / d0 * 0.1D;
					fireballentity.explosionPower = explosionStrength;
					world.addEntity(fireballentity);
				}
			}
		}
	}


}
