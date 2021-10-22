package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpPlantEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.Random;

public final class FlamingPlantBehavior implements IGameBehavior {
    public static final Codec<FlamingPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("radius").forGetter(b -> b.radius)
    ).apply(instance, FlamingPlantBehavior::new));
    private final int radius;

    private IGamePhase game;

    public FlamingPlantBehavior(int radius) {
        this.radius = radius;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;
        events.listen(MpPlantEvents.TICK, this::tickPlants);
    }

    private void tickPlants(ServerPlayerEntity player, Plot plot, List<Plant> plants) {
        long ticks = this.game.ticks();
        Random random = this.game.getWorld().getRandom();

        if (ticks % 5 != 0) {
            return;
        }

        ServerWorld world = this.game.getWorld();

        for (Plant plant : plants) {
            AxisAlignedBB flameBounds = plant.coverage().asBounds().grow(this.radius);
            List<MobEntity> entities = world.getEntitiesWithinAABB(MobEntity.class, flameBounds, entity -> !(entity instanceof VillagerEntity));

            for (MobEntity entity : entities) {
                entity.setFire(3);
            }

            BlockPos pos = plant.coverage().getOrigin();
            if (random.nextInt(3) == 0) {
                int count = random.nextInt(3);
                for(int i = 0; i < count; ++i) {
                    double d3 = random.nextGaussian() * 0.05;
                    double d1 = random.nextGaussian() * 0.1;
                    double d2 = random.nextGaussian() * 0.05;
                    world.spawnParticle(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 1 + random.nextInt(2), d3, d1, d2, 0.03 + random.nextDouble() * 0.02);
                }
            }
        }
    }
}
