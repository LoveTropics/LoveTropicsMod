package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

public final class BbMobSpawner {
    public static Set<Entity> spawnWaveEntities(ServerWorld world, Random random, Plot plot, int count, int waveIndex, WaveSelector waveSelector) {
        Set<Entity> entities = Collections.newSetFromMap(new WeakHashMap<>());
        for (int i = 0; i < count; i++) {
            BlockPos pos = plot.mobSpawn.sample(random);

            MobEntity entity = waveSelector.selectEntityForWave(random, world, plot, waveIndex);

            Direction direction = plot.forward.getOpposite();
            entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, direction.getHorizontalAngle(), 0);

            world.addEntity(entity);

            entity.onInitialSpawn(world, world.getDifficultyForLocation(pos), SpawnReason.MOB_SUMMONED, null, null);
            entities.add(entity);
        }

        return entities;
    }

    // TODO: data-drive, more entity types & getting harder as time goes on
    public static MobEntity selectEntityForWave(Random random, World world, Plot plot, int waveIndex) {
        if (random.nextInt(8) == 0 && waveIndex > 4) {
            return new BbCreeperEntity(EntityType.CREEPER, world, plot);
        }

        if (random.nextInt(3) == 0 && waveIndex > 2) {
            return new BbPillagerEntity(EntityType.PILLAGER, world, plot);
        }

        return new BbHuskEntity(EntityType.HUSK, world, plot);
    }

    @FunctionalInterface
    public interface WaveSelector {
        MobEntity selectEntityForWave(Random random, World world, Plot plot, int waveIndex);
    }
}
