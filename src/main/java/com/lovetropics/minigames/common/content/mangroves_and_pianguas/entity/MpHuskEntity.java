package com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity.ai.MoveToPumpkinGoal;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity.ai.ScaredGroundNavigator;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.monster.HuskEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.world.World;

public class MpHuskEntity extends HuskEntity implements ScareableEntity {
    private final ScareManager scares = new ScareManager(2);

    public MpHuskEntity(EntityType<? extends HuskEntity> type, World worldIn) {
        super(type, worldIn);

        // Ignore sweet berry bushes and water
        this.setPathPriority(PathNodeType.DANGER_OTHER, 0.0F);
        this.setPathPriority(PathNodeType.DAMAGE_OTHER, 0.0F);
        this.setPathPriority(PathNodeType.WATER, -1.0F);
    }

    @Override
    protected PathNavigator createNavigator(World world) {
        return new ScaredGroundNavigator(this, this, world);
    }

    @Override
    protected void applyEntityAI() {
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0D, false));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
        this.goalSelector.addGoal(1, new MoveToPumpkinGoal(this));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setCallsForHelp(ZombifiedPiglinEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
    }

    @Override
    public ScareManager getScareManager() {
        return scares;
    }
}
