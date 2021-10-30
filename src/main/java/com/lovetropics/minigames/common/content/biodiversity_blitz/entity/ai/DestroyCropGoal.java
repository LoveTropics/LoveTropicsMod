package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantHealth;
import net.minecraft.block.BlockState;
import net.minecraft.entity.MobEntity;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

public class DestroyCropGoal extends MoveToBlockGoal {
    private final BbMobEntity mob;
    private int ticksAtTarget = 30;

    public DestroyCropGoal(BbMobEntity mob) {
        super(mob.asMob());
        this.mob = mob;
    }

    @Override
    public void tick() {
        super.tick();

        MobEntity mob = this.mob.asMob();
        if (mob.getPosition().distanceSq(this.targetPos) <= 2 * 2) {
            this.ticksAtTarget--;

            if (this.ticksAtTarget <= 0) {
                this.ticksAtTarget = 30;

                if (this.mob.getPlot().plants.hasPlantAt(this.targetPos)) {
                    PlantHealth health = this.mob.getPlot().plants.getPlantAt(this.targetPos).state(PlantHealth.KEY);

                    if (health != null) {
                        health.decrement(8);

                        Random random = mob.world.rand;
                        for(int i = 0; i < 6; ++i) {
                            double d0 = random.nextGaussian() * 0.02D;
                            double d1 = random.nextGaussian() * 0.02D;
                            double d2 = random.nextGaussian() * 0.02D;

                            ((ServerWorld)mob.world).spawnParticle(ParticleTypes.ANGRY_VILLAGER, this.targetPos.getX() + 0.5, this.targetPos.getY() + 0.5, this.targetPos.getZ() + 0.5, 1 + random.nextInt(2), d0, d1, d2, 0.01 + random.nextDouble() * 0.02);
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldExecute() {
        if (super.shouldExecute()) {
//            this.ticksAtTarget = 30;
            return true;
        }

        return false;
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
    }

    @Override
    public void resetTask() {
        super.resetTask();
    }

    @Override
    protected boolean shouldContinueExecuting(BlockPos pos) {
        return mob.getPlot().plants.hasPlantAt(pos) && this.mob.getPlot().plants.getPlantAt(pos).state(PlantHealth.KEY) != null;
    }

    @Override
    protected boolean isValidBlock(BlockPos pos, BlockState state) {
        return mob.getPlot().plants.hasPlantAt(pos) && this.mob.getPlot().plants.getPlantAt(pos).state(PlantHealth.KEY) != null;
    }
}
