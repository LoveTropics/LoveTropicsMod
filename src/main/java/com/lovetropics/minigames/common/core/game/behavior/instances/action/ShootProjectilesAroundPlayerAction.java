package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.Iterator;

public class ShootProjectilesAroundPlayerAction implements IGameBehavior {
	public static final MapCodec<ShootProjectilesAroundPlayerAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.INT.optionalFieldOf("entity_count_per_player", 10).forGetter(c -> c.entityCountPerPlayer),
			Codec.INT.optionalFieldOf("spawn_distance_max", 40).forGetter(c -> c.spawnDistanceMax),
			Codec.INT.optionalFieldOf("target_randomness", 10).forGetter(c -> c.targetRandomness),
			Codec.INT.optionalFieldOf("spawn_height", 20).forGetter(c -> c.spawnRangeY),
			Codec.INT.optionalFieldOf("spawn_rate_base", 20).forGetter(c -> c.spawnRateBase),
			Codec.INT.optionalFieldOf("spawn_rate_random", 20).forGetter(c -> c.spawnRateRandom),
			Codec.INT.optionalFieldOf("explosion_strength", 2).forGetter(c -> c.explosionStrength)
	).apply(i, ShootProjectilesAroundPlayerAction::new));

	//private final ResourceLocation entityId;
	private final int entityCountPerPlayer;
	private final int spawnDistanceMax;
	private final int targetRandomness;
	private final int spawnRangeY;
	private final int spawnRateBase;
	private final int spawnRateRandom;
	private final int explosionStrength;
	private final Object2IntMap<ServerPlayer> playerToAmountToSpawn = new Object2IntOpenHashMap<>();
	private final Object2IntMap<ServerPlayer> playerToDelayToSpawn = new Object2IntOpenHashMap<>();

	public ShootProjectilesAroundPlayerAction(/*final ResourceLocation entityId, */final int entityCount, final int spawnDistanceMax, final int spawnRangeY, final int spawnsPerTickBase, final int spawnsPerTickRandom, final int targetRandomness, final int explosionStrength) {
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
					ServerLevel world = game.getWorld();
					RandomSource random = world.getRandom();

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
					LargeFireball fireball = createFireball(world, posSpawn, posTarget);
					world.addFreshEntity(fireball);
				}
			}
		}
	}

	private LargeFireball createFireball(ServerLevel world, BlockPos spawn, BlockPos target) {
		double deltaX = target.getX() - spawn.getX();
		double deltaY = target.getY() - spawn.getY();
		double deltaZ = target.getZ() - spawn.getZ();
		double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);

		LargeFireball fireball = new LargeFireball(EntityType.FIREBALL, world) {
			@Override
			protected void onHit(final HitResult hitResult) {
				HitResult.Type resultType = hitResult.getType();
				switch (resultType) {
					case ENTITY -> onHitEntity((EntityHitResult) hitResult);
					case BLOCK -> onHitBlock((BlockHitResult) hitResult);
				}

				if (!level().isClientSide) {
					final boolean mobGriefing = ForgeEventFactory.getMobGriefingEvent(level(), getOwner());
					level().explode(null, getX(), getY(), getZ(), explosionStrength, mobGriefing, Level.ExplosionInteraction.MOB);
					discard();
				}
			}
		};

		fireball.moveTo(spawn.getX(), spawn.getY(), spawn.getZ(), fireball.getYRot(), fireball.getXRot());
		fireball.setPos(spawn.getX(), spawn.getY(), spawn.getZ());
		fireball.xPower = deltaX / distance * 0.1;
		fireball.yPower = deltaY / distance * 0.1;
		fireball.zPower = deltaZ / distance * 0.1;

		return fireball;
	}
}
