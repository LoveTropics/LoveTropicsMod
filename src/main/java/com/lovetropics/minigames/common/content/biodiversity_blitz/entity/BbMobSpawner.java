package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.impl.BbCreeperEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.impl.BbHuskEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.impl.BbPillagerEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;

public final class BbMobSpawner {
    public static Set<Entity> spawnWaveEntities(ServerLevel world, RandomSource random, Plot plot, int count, int waveIndex, WaveSelector waveSelector, BbEvents.ModifyWaveMobs modifier) {
        Set<Entity> entities = Collections.newSetFromMap(new WeakHashMap<>());
        PlotWaveState waveState = plot.waveState;

        waveState.didForcedCreeperSpawn = false;
        for (int i = 0; i < count; i++) {

            Mob entity = waveSelector.selectEntityForWave(random, world, plot, waveIndex);

            entities.add(entity);
        }

        modifier.modifyWave(entities, random, world, plot, waveIndex);

        waveState.didCreeperSpawnLastWave = false;

        for (Entity entity : entities) {
            if (entity instanceof BbCreeperEntity) {
                waveState.didCreeperSpawnLastWave = true;
            }

            BlockPos pos = plot.mobSpawn.sample(random);
            Direction direction = plot.forward.getOpposite();
            entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, direction.toYRot(), 0);

            world.addFreshEntity(entity);

            ((Mob)entity).finalizeSpawn(world, world.getCurrentDifficultyAt(pos), MobSpawnType.MOB_SUMMONED, null, null);
        }

        return entities;
    }

    // TODO: data-drive, more entity types & getting harder as time goes on
    public static Mob selectEntityForWave(RandomSource random, Level world, Plot plot, int waveIndex) {
        PlotWaveState waveState = plot.waveState;
        if (!waveState.didForcedCreeperSpawn && plot.nextCurrencyIncrement >= 14) {
            waveState.didForcedCreeperSpawn = true;
            return new BbCreeperEntity(EntityType.CREEPER, world, plot);
        }

        if (random.nextInt(7) == 0 && waveIndex > 4 && plot.nextCurrencyIncrement >= 8 && !waveState.didCreeperSpawnLastWave) {
            return new BbCreeperEntity(EntityType.CREEPER, world, plot);
        }

        if (random.nextInt(3) == 0 && waveIndex > 2 && plot.nextCurrencyIncrement >= 5) {
            return new BbPillagerEntity(EntityType.PILLAGER, world, plot);
        }

        return new BbHuskEntity(EntityType.HUSK, world, plot);
    }

    @FunctionalInterface
    public interface WaveSelector {
        Mob selectEntityForWave(RandomSource random, Level world, Plot plot, int waveIndex);
    }

    public enum BbEntityTypes implements StringRepresentable {
        CREEPER(EntityType.CREEPER, BbCreeperEntity::new),
        PILLAGER(EntityType.PILLAGER, BbPillagerEntity::new),
        HUSK(EntityType.HUSK, BbHuskEntity::new);

        public static final Codec<BbEntityTypes> CODEC = StringRepresentable.fromEnum(BbEntityTypes::values);

        private final EntityType<?> entityType;
        private final Creator<?> creator;
        <T extends Mob> BbEntityTypes(EntityType<T> entityType, Creator<T> creator) {
            this.creator = creator;
            this.entityType = entityType;
        }

        public Mob create(Level level, Plot plot) {
            return creator.create((EntityType)entityType, level, plot);
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public MutableComponent getName() {
            return Component.translatable(getTranslationKey());
        }

        public String getTranslationKey() {
            return "ltminigames.bb_entities." + getSerializedName();
        }

        interface Creator<T extends Mob> {
            Mob create(EntityType<T> type, Level level, Plot plot);
        }
    }
}
