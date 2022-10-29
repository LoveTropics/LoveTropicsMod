package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbTargetPlayerGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.DestroyCropGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.MoveToPumpkinGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.Level;

// Husk that cannot ever attack the player
public class BbTutorialHuskEntity extends BbHuskEntity {
    public BbTutorialHuskEntity(EntityType<? extends Husk> type, Level world, Plot plot) {
        super(type, world, plot);
    }

    @Override
    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(1, new MoveToPumpkinGoal(this));
        this.goalSelector.addGoal(3, new DestroyCropGoal(this));
    }
}
