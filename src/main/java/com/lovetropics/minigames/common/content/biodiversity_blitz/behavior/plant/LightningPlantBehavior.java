package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public final class LightningPlantBehavior implements IGameBehavior {
    public static final Codec<LightningPlantBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("radius").forGetter(b -> b.radius)
    ).apply(i, LightningPlantBehavior::new));
    private final int radius;

    private IGamePhase game;

    public LightningPlantBehavior(int radius) {
        this.radius = radius;
    }

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        this.game = game;
        events.listen(BbPlantEvents.TICK, this::tickPlants);
    }

    private void tickPlants(Collection<ServerPlayer> players, Plot plot, List<Plant> plants) {
        long ticks = this.game.ticks();
        RandomSource random = this.game.getWorld().getRandom();

        if (ticks % 160 != 0) {
            return;
        }

        ServerLevel world = this.game.getWorld();

        for (Plant plant : plants) {
            AABB flameBounds = plant.coverage().asBounds().inflate(this.radius);
            List<Mob> entities = world.getEntitiesOfClass(Mob.class, flameBounds, BbMobEntity.PREDICATE);

            // Select random entity and spawn lightning on top of it.
            if (!entities.isEmpty()) {
                Mob target = entities.get(random.nextInt(entities.size()));

                BlockPos pos = target.blockPosition();
                // TODO: custom lightning bolt class to prevent too loud sounds and fire!
                LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(world);
                lightningbolt.moveTo(Vec3.atBottomCenterOf(pos));
                lightningbolt.setCause(null);
                world.addFreshEntity(lightningbolt);
            }
        }
    }
}
