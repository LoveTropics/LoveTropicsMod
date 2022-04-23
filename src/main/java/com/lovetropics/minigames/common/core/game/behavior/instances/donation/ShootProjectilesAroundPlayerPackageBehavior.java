package com.lovetropics.minigames.common.core.game.behavior.instances.donation;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
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
import java.util.Random;

public class ShootProjectilesAroundPlayerPackageBehavior implements IGameBehavior {
	public static final Codec<ShootProjectilesAroundPlayerPackageBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.optionalFieldOf("entity_count_per_player", 10).forGetter(c -> c.entityCountPerPlayer),
				Codec.INT.optionalFieldOf("spawn_distance_max", 40).forGetter(c -> c.spawnDistanceMax),
				Codec.INT.optionalFieldOf("target_randomness", 10).forGetter(c -> c.targetRandomness),
				Codec.INT.optionalFieldOf("spawn_height", 20).forGetter(c -> c.spawnRangeY),
				Codec.INT.optionalFieldOf("spawn_rate_base", 20).forGetter(c -> c.spawnRateBase),
				Codec.INT.optionalFieldOf("spawn_rate_random", 20).forGetter(c -> c.spawnRateRandom),
				Codec.INT.optionalFieldOf("explosion_strength", 2).forGetter(c -> c.explosionStrength)
		).apply(instance, ShootProjectilesAroundPlayerPackageBehavior::new);
	});

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

	public ShootProjectilesAroundPlayerPackageBehavior(/*final ResourceLocation entityId, */final int entityCount, final int spawnDistanceMax, final int spawnRangeY, final int spawnsPerTickBase, final int spawnsPerTickRandom, final int targetRandomness, final int explosionStrength) {
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
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePackageEvents.APPLY_PACKAGE_TO_PLAYER, (player, sendingPlayer) -> {
			playerToAmountToSpawn.put(player, entityCountPerPlayer);
			return true;
		});
		events.listen(GamePhaseEvents.TICK, () -> tick(game));
	}

	private void tick(IGamePhase game) {
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
					ServerWorld world = game.getWorld();
					Random random = world.getRandom();

					cooldown = spawnRateBase + random.nextInt(spawnRateRandom);
					playerToDelayToSpawn.put(entry.getKey(), cooldown);

					BlockPos posSpawn = entry.getKey().blockPosition().offset(random.nextInt(spawnDistanceMax * 2) - spawnDistanceMax, 20,
							random.nextInt(spawnDistanceMax * 2) - spawnDistanceMax);

					BlockPos posTarget = entry.getKey().blockPosition().offset(random.nextInt(targetRandomness * 2) - targetRandomness, 0,
							random.nextInt(targetRandomness * 2) - targetRandomness);

					entry.setValue(entry.getIntValue() - 1);
					if (entry.getIntValue() <= 0) {
						playerToDelayToSpawn.removeInt(entry.getKey());
						it.remove();
					}
					FireballEntity fireball = createFireball(world, posSpawn, posTarget);
					world.addFreshEntity(fireball);
				}
			}
		}
	}

	private FireballEntity createFireball(ServerWorld world, BlockPos spawn, BlockPos target) {
		double deltaX = target.getX() - spawn.getX();
		double deltaY = target.getY() - spawn.getY();
		double deltaZ = target.getZ() - spawn.getZ();
		double distance = MathHelper.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

		FireballEntity fireball = new FireballEntity(EntityType.FIREBALL, world);
		fireball.moveTo(spawn.getX(), spawn.getY(), spawn.getZ(), fireball.yRot, fireball.xRot);
		fireball.setPos(spawn.getX(), spawn.getY(), spawn.getZ());
		fireball.xPower = deltaX / distance * 0.1;
		fireball.yPower = deltaY / distance * 0.1;
		fireball.zPower = deltaZ / distance * 0.1;
		fireball.explosionPower = explosionStrength;

		return fireball;
	}
}
