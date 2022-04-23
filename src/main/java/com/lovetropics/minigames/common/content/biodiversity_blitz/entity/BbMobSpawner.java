package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.WeakHashMap;

public final class BbMobSpawner {
    public static Set<Entity> spawnWaveEntities(ServerLevel world, Random random, Plot plot, int count, int waveIndex, WaveSelector waveSelector) {
        Set<Entity> entities = Collections.newSetFromMap(new WeakHashMap<>());
        for (int i = 0; i < count; i++) {
            BlockPos pos = plot.mobSpawn.sample(random);

            Mob entity = waveSelector.selectEntityForWave(random, world, plot, waveIndex);

            Direction direction = plot.forward.getOpposite();
            entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, direction.toYRot(), 0);

            world.addFreshEntity(entity);

            entity.finalizeSpawn(world, world.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null, null);
            entities.add(entity);
        }

        return entities;
    }

    // TODO: data-drive, more entity types & getting harder as time goes on
    public static Mob selectEntityForWave(Random random, Level world, Plot plot, int waveIndex) {
        if (random.nextInt(8) == 0 && waveIndex > 4 && plot.nextCurrencyIncrement >= 10) {
            return new BbCreeperEntity(EntityType.CREEPER, world, plot);
        }

        if (random.nextInt(3) == 0 && waveIndex > 2 && plot.nextCurrencyIncrement >= 5) {
            return new BbPillagerEntity(EntityType.PILLAGER, world, plot);
        }

        return new BbHuskEntity(EntityType.HUSK, world, plot);
    }

    @FunctionalInterface
    public interface WaveSelector {
        Mob selectEntityForWave(Random random, Level world, Plot plot, int waveIndex);
    }
}
