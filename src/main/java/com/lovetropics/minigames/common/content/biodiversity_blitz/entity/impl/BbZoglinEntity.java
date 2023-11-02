package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.impl;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbGroundNavigator;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbMobBrain;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbTargetPlayerGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.DestroyCropGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantHealth;
import com.lovetropics.minigames.common.util.Util;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class BbZoglinEntity extends Zoglin implements BbMobEntity {
    private final BbMobBrain mobBrain;
    private final Plot plot;

    private int ticks;

    public BbZoglinEntity(EntityType<? extends Zoglin> pEntityType, Level pLevel, Plot plot) {
        super(pEntityType, pLevel);

        this.mobBrain = new BbMobBrain(plot.walls);
        this.plot = plot;

        setPathfindingMalus(BlockPathTypes.DANGER_OTHER, 0.0F);
        setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
    }

    @Override
    public boolean navigateBlockGrid() {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        return new BbGroundNavigator(this);
    }

    @Override
    protected void registerGoals() {
        // Remove Brain, replace with Goal
        this.removeFreeWill();

        this.goalSelector.addGoal(3, new DestroyCropGoal(this) {
            @Override
            protected double getDistanceSq(BlockState state) {
                return 2.5 * 2.5;
            }
        });

        this.targetSelector.addGoal(1, new BbTargetPlayerGoal(this));
    }

    @Override
    public void aiStep() {
        ticks++;
        if (ticks % 7 == 0) {
            Plant plant = getPlot().plants.getPlantAt(this.blockPosition());

            if (plant != null) {
                PlantHealth health = plant.state(PlantHealth.KEY);

                if (health != null) {
                    health.decrement(meleeDamage(this.random));

                    Util.spawnDamageParticles(this, this.blockPosition(), 4);
                }
            }
        }

        super.aiStep();
    }

    @Override
    protected Vec3 maybeBackOffFromEdge(Vec3 offset, MoverType mover) {
        return mobBrain.getPlotWalls().collide(this.getBoundingBox(), offset);
    }

    @Override
    public BbMobBrain getMobBrain() {
        return this.mobBrain;
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
    public int meleeDamage(RandomSource random) {
        return 12 + BbMobEntity.super.meleeDamage(random);
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public double aiSpeed() {
        return 1.0;
    }

    @Override
    public boolean immuneToFire() {
        return true;
    }
}
