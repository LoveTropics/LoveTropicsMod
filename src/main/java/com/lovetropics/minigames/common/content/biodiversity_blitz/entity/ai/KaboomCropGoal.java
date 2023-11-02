package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.impl.BbCreeperEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantHealth;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.state.BlockState;

public class KaboomCropGoal extends DestroyCropGoal {
    private final BbCreeperEntity mob;

    public KaboomCropGoal(BbCreeperEntity mob) {
        super(mob);
        this.mob = mob;
    }

    @Override
    protected double getDistanceSq(BlockState state) {
        // Longer reach distance
        return 2 * 2;
    }

    @Override
    protected void tryDamagePlant(Mob mob) {
        int inc = this.mob.getPlot().nextCurrencyIncrement;
        float offset = 0;

        // Scale the creeper explode size based on how well the players are doing
        if (inc < 3) {
            offset = -0.25f;
        } else if (inc >= 8) {
            offset = 1.0f;
        } else if (inc >= 5) {
            offset = 0.5f;
        }

        this.mob.setCreeperExplodeSizeOffset(offset);

        // Tell creeper to explode
        this.mob.setSwellDir(1);
    }

    @Override
    public void tick() {
        super.tick();

        // Extra evil-- if the team is making a yachtload of points per turn, the creeper should simply explode when low health
        if (this.mob.getPlot().nextCurrencyIncrement >= 14) {
            if (this.mob.getHealth() < 8) {
                this.mob.setCreeperExplodeSizeOffset(0.75f);

                this.mob.setSwellDir(1);
            }
        }
    }

    @Override
    protected int getPlantPriority(BlockPos pos) {
        Plant plant = this.mob.getPlot().plants.getPlantAt(pos);
        if (plant != null) {
            PlantHealth state = plant.state(PlantHealth.KEY);

            // TODO:
            if (state != null) {
                return state.health();
            }
        }

        return 0;
    }
}
