package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbCreeperEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantHealth;
import net.minecraft.world.entity.Mob;
import net.minecraft.core.BlockPos;

public class KaboomCropGoal extends DestroyCropGoal {
    private final BbCreeperEntity mob;

    public KaboomCropGoal(BbCreeperEntity mob) {
        super(mob);
        this.mob = mob;
    }

    @Override
    protected double getDistanceSq() {
        // Longer reach distance
        return 2 * 2;
    }

    @Override
    protected void tryDamagePlant(Mob mob) {
        // Tell creeper to explode
        this.mob.setSwellDir(1);
    }

    @Override
    protected boolean isPlantBreakable(BlockPos pos) {
        Plant plant = this.mob.getPlot().plants.getPlantAt(pos);
        return plant != null && plant.state(PlantHealth.KEY) != null;
    }
}
