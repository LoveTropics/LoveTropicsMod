package com.lovetropics.minigames.common.minigames.behaviours.instances.donations;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.common.game_actions.GamePackage;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigamePackageBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

import java.util.Iterator;
import java.util.List;

public class ShootProjectilesAroundAllPlayersPackageBehavior implements IMinigamePackageBehavior
{
	public static final Codec<ShootProjectilesAroundAllPlayersPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DonationPackageData.CODEC.forGetter(c -> c.data),
				Codec.INT.optionalFieldOf("entity_count_per_player", 10).forGetter(c -> c.entityCountPerPlayer),
				Codec.INT.optionalFieldOf("spawn_distance_max", 40).forGetter(c -> c.spawnDistanceMax),
				Codec.INT.optionalFieldOf("target_randomness", 10).forGetter(c -> c.targetRandomness),
				Codec.INT.optionalFieldOf("spawn_height", 20).forGetter(c -> c.spawnRangeY),
				Codec.INT.optionalFieldOf("spawn_rate_base", 20).forGetter(c -> c.spawnRateBase),
				Codec.INT.optionalFieldOf("spawn_rate_random", 20).forGetter(c -> c.spawnRateRandom),
				Codec.INT.optionalFieldOf("explosion_strength", 2).forGetter(c -> c.explosionStrength)
		).apply(instance, ShootProjectilesAroundAllPlayersPackageBehavior::new);
	});

	private final DonationPackageData data;
	//private final ResourceLocation entityId;
	private final int entityCountPerPlayer;
	private final int spawnDistanceMax;
	private final int targetRandomness;
	private final int spawnRangeY;
	private final int spawnRateBase;
	private final int spawnRateRandom;
	private final int explosionStrength;

	private final Object2IntMap<ServerPlayerEntity> playerToAmountToSpawn = new Object2IntOpenHashMap<>();
	private final Object2IntMap<ServerPlayerEntity> playerToDelayToSpawn = new Object2IntOpenHashMap<>();

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
	public String getPackageType() {
		return data.getPackageType();
	}

	@Override
	public boolean onGamePackageReceived(final IMinigameInstance minigame, final GamePackage gamePackage) {
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
	public void worldUpdate(IMinigameInstance minigame, ServerWorld world) {
		Iterator<Object2IntMap.Entry<ServerPlayerEntity>> it = playerToAmountToSpawn.object2IntEntrySet().iterator();
		while (it.hasNext()) {
			Object2IntMap.Entry<ServerPlayerEntity> entry = it.next();

			if (!entry.getKey().isAlive()) {
				it.remove();
				playerToDelayToSpawn.removeInt(entry.getKey());
			} else {

				int cooldown = 0;
				if (playerToDelayToSpawn.containsKey(entry.getKey())) {
					cooldown = playerToDelayToSpawn.getInt(entry.getKey());
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

					entry.setValue(entry.getIntValue() - 1);
					if (entry.getIntValue() <= 0) {
						it.remove();
						playerToDelayToSpawn.removeInt(entry.getKey());
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
