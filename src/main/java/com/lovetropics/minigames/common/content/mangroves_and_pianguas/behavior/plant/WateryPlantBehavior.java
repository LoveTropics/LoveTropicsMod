package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class WateryPlantBehavior implements IGameBehavior {
    public static final Codec<WateryPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("radius").forGetter(b -> b.radius)
    ).apply(instance, WateryPlantBehavior::new));
    private final int radius;

    private IGamePhase game;

    public WateryPlantBehavior(int radius) {
        this.radius = radius;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;
        events.listen(MpEvents.TICK_PLANTS, this::tickPlants);
    }

    private void tickPlants(ServerPlayerEntity player, Plot plot, List<Plant> plants) {
        long ticks = this.game.ticks();
        Random random = this.game.getWorld().getRandom();

        if (ticks % 5 != 0) {
            return;
        }

        ServerWorld world = this.game.getWorld();
        Set<MobEntity> seen = new HashSet<>();

        for (Plant plant : plants) {
            AxisAlignedBB flameBounds = plant.coverage().asBounds().grow(this.radius);
            List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, flameBounds, entity -> !(entity instanceof VillagerEntity));

            if (entities.isEmpty()) {
                continue;
            }

            for (MobEntity entity : entities) {
                // Don't attack the same entity multiple times
                if (seen.contains(entity)) {
                    continue;
                }

                seen.add(entity);

                int waterCount = 2 + random.nextInt(3);

                if (ticks % 15 == 0) {
                    entity.attackEntityFrom(DamageSource.MAGIC, 2 + random.nextInt(3));
                    waterCount += 5 + random.nextInt(8);
                }

                // Don't add particles to mobs that should be dead
                if (entity.getShouldBeDead()) {
                    continue;
                }

                for (int i = 0; i < waterCount; i++) {
                    Vector3d sample = random(entity.getBoundingBox(), world.rand);
                    double d3 = random.nextGaussian() * 0.05;
                    double d1 = random.nextGaussian() * 0.1;
                    double d2 = random.nextGaussian() * 0.05;
                    world.spawnParticle(ParticleTypes.FALLING_WATER, sample.x, sample.y, sample.z, 1 + random.nextInt(2), d3, d1, d2, 0.03 + random.nextDouble() * 0.02);
                }
            }
        }
    }

    private static Vector3d random(AxisAlignedBB aabb, Random random) {
        return new Vector3d(
                aabb.minX + random.nextDouble() * (aabb.maxX - aabb.minX),
                aabb.minY + random.nextDouble() * (aabb.maxY - aabb.minY),
                aabb.minZ + random.nextDouble() * (aabb.maxZ - aabb.minZ)
        );
    }
}