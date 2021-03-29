package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.game.IGameInstance;
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

public class ShootProjectilesAroundPlayerPackageBehavior extends DonationPackageBehavior
{
	public static final Codec<ShootProjectilesAroundPlayerPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				DonationPackageData.CODEC.forGetter(c -> c.data),
				Codec.INT.optionalFieldOf("entity_count_per_player", 10).forGetter(c -> c.entityCountPerPlayer),
				Codec.INT.optionalFieldOf("spawn_distance_max", 40).forGetter(c -> c.spawnDistanceMax),
				Codec.INT.optionalFieldOf("target_randomness", 10).forGetter(c -> c.targetRandomness),
				Codec.INT.optionalFieldOf("spawn_height", 20).forGetter(c -> c.spawnRangeY),
				Codec.INT.optionalFieldOf("spawn_rate_base", 20).forGetter(c -> c.spawnRateBase),
				Codec.INT.optionalFieldOf("spawn_rate_random", 20).forGetter(c -> c.spawnRateRandom),
				Codec.INT.optionalFieldOf("explosion_strength", 2).forGetter(c -> c.explosionStrength)
		).apply(instance, ShootProjectilesAroundPlayerPackageBehavior::new);
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

	public ShootProjectilesAroundPlayerPackageBehavior(final DonationPackageData data, /*final ResourceLocation entityId, */final int entityCount, final int spawnDistanceMax, final int spawnRangeY, final int spawnsPerTickBase, final int spawnsPerTickRandom, final int targetRandomness, final int explosionStrength) {
		super(data);
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
	protected void receivePackage(final String sendingPlayer, final ServerPlayerEntity player) {
		playerToAmountToSpawn.put(player, entityCountPerPlayer);
	}

	@Override
	public void worldUpdate(IGameInstance minigame, ServerWorld world) {
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
