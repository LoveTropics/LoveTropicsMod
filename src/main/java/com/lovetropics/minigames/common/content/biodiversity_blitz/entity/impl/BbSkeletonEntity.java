package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.impl;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbMobBrain;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbTargetPlayerGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.DestroyCropGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

public class BbSkeletonEntity extends Skeleton implements BbMobEntity {
    private final BbMobBrain mobBrain;
    private final Plot plot;

    public BbSkeletonEntity(EntityType<? extends Skeleton> p_33570_, Level level, Plot plot) {
        super(p_33570_, level);

        this.mobBrain = new BbMobBrain(plot.walls);
        this.plot = plot;

        setPathfindingMalus(BlockPathTypes.DANGER_OTHER, BERRY_BUSH_MALUS);
        setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new DestroyCropGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 15.0F, 0.02F));

        this.targetSelector.addGoal(1, new BbTargetPlayerGoal(this));

        // Setup bow goal

        // TODO: is this the right spot for this?
        populateDefaultEquipmentSlots(random, new DifficultyInstance(Difficulty.NORMAL, 0, 0, 0));
        reassessWeaponGoal();
    }

    @Override
    public BbMobBrain getMobBrain() {
        return mobBrain;
    }

    @Override
    public Mob asMob() {
        return this;
    }

    @Override
    public Plot getPlot() {
        return this.plot;
    }

    @Override
    protected void pushEntities() {
    }
}
