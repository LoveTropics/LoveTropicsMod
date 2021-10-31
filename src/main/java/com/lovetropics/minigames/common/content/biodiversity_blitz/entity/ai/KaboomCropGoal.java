package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbCreeperEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import net.minecraft.entity.MobEntity;

public class KaboomCropGoal extends DestroyCropGoal {
    private final BbCreeperEntity mob;

    public KaboomCropGoal(BbCreeperEntity mob) {
        super(mob);
        this.mob = mob;
    }

    @Override
    protected boolean checkForNearbyPlayer() {
        return false; // Creeper can and will explode right next to you :)
    }

    @Override
    protected double getDistanceSq() {
        // Longer reach distance
        return 2 * 2;
    }

    @Override
    protected void tryDamagePlant(MobEntity mob) {
        // Tell creeper to explode
        this.mob.setCreeperState(1);
    }
}
