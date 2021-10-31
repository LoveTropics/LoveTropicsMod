package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantHealth;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantNotPathfindable;
import net.minecraft.entity.MobEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class DestroyCropGoal extends MoveToBlockGoal {
    private static final int DAMAGE_VALUE = 8;
    private static final int DAMAGE_INTERVAL = 30;

    private final BbMobEntity mob;
    private int ticksAtTarget = DAMAGE_INTERVAL;

    public DestroyCropGoal(BbMobEntity mob) {
        super(mob.asMob());
        this.mob = mob;
    }

    @Override
    public void tick() {
        super.tick();

        MobEntity mob = this.mob.asMob();
        double distance2 = mob.getPositionVec().squareDistanceTo(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        if (distance2 <= getDistanceSq()) {
            this.ticksAtTarget--;
            if (this.ticksAtTarget <= 0) {
                this.ticksAtTarget = DAMAGE_INTERVAL;
                this.tryDamagePlant(mob);
            }
        }
    }

    protected double getDistanceSq() {
        return 1.5 * 1.5;
    }

    protected void tryDamagePlant(MobEntity mob) {
        Plant plant = this.mob.getPlot().plants.getPlantAt(this.targetPos);
        if (plant != null) {
            PlantHealth health = plant.state(PlantHealth.KEY);

            if (health != null) {
                health.decrement(DAMAGE_VALUE);

                this.spawnDamageParticles(mob);
            }
        }
    }

    private void spawnDamageParticles(MobEntity mob) {
        Random random = mob.world.rand;
        for (int i = 0; i < 6; ++i) {
            double dx = random.nextGaussian() * 0.02;
            double dy = random.nextGaussian() * 0.02;
            double dz = random.nextGaussian() * 0.02;

            ((ServerWorld) mob.world).spawnParticle(
                    ParticleTypes.ANGRY_VILLAGER,
                    this.targetPos.getX() + 0.5,
                    this.targetPos.getY() + 0.5,
                    this.targetPos.getZ() + 0.5,
                    1 + random.nextInt(2),
                    dx, dy, dz,
                    0.01 + random.nextDouble() * 0.02
            );
        }
    }

    @Override
    protected boolean shouldTargetBlock(BlockPos pos) {
        return this.isPlantBreakable(pos);
    }

    protected boolean isPlantBreakable(BlockPos pos) {
        Plant plant = this.mob.getPlot().plants.getPlantAt(pos);
        return plant != null && plant.state(PlantHealth.KEY) != null && plant.state(PlantNotPathfindable.KEY) == null;
    }
}
