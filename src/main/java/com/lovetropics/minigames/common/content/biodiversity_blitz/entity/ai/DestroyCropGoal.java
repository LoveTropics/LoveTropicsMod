package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantHealth;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantNotPathfindable;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

public class DestroyCropGoal extends MoveToBlockGoal {
    private static final int DAMAGE_INTERVAL = 25;

    private final BbMobEntity mob;
    private int ticksAtTarget = DAMAGE_INTERVAL;

    public DestroyCropGoal(BbMobEntity mob) {
        super(mob.asMob());
        this.mob = mob;
    }

    @Override
    public void tick() {
        super.tick();

        Mob mob = this.mob.asMob();
        double distance2 = mob.position().distanceToSqr(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        if (distance2 <= getDistanceSq(mob.level.getBlockState(targetPos))) {
            this.ticksAtTarget--;
            if (mob.level.random.nextInt(4) == 0) {
                spawnDamageParticles(mob, 0);
            }
            
            if (this.ticksAtTarget <= 0) {
                this.ticksAtTarget = DAMAGE_INTERVAL;
                this.tryDamagePlant(mob);
            }
        } else {
            this.ticksAtTarget = DAMAGE_INTERVAL;
        }
    }

    protected double getDistanceSq(BlockState state) {
        return state.getMaterial().isSolid() ? 1.5 * 1.5 : 0.5 * 0.5;
    }

    protected void tryDamagePlant(Mob mob) {
        Plant plant = this.mob.getPlot().plants.getPlantAt(this.targetPos);
        if (plant != null) {
            PlantHealth health = plant.state(PlantHealth.KEY);

            if (health != null) {
                int damage = 3 + mob.level.random.nextInt(6);
                health.decrement(damage);

                this.spawnDamageParticles(mob, damage);
            }
        }
    }

    private void spawnDamageParticles(Mob mob, int damage) {
        Random random = mob.level.random;
        for (int i = 0; i < 2 + (damage / 2); ++i) {
            double dx = random.nextGaussian() * 0.02;
            double dy = random.nextGaussian() * 0.02;
            double dz = random.nextGaussian() * 0.02;

            ((ServerLevel) mob.level).sendParticles(
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
