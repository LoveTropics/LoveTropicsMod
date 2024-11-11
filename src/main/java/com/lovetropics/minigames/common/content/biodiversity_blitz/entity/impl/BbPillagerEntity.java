package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.impl;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbGroundNavigator;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbMobBrain;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbTargetPlayerGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.DestroyCropGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Pillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class BbPillagerEntity extends Pillager implements BbMobEntity {
    private final BbMobBrain mobBrain;
    private final Plot plot;

    public BbPillagerEntity(EntityType<? extends Pillager> type, Level world, Plot plot) {
        super(type, world);
        mobBrain = new BbMobBrain(plot.walls);
        this.plot = plot;

        setPathfindingMalus(PathType.DANGER_OTHER, BERRY_BUSH_MALUS);
    }

    @Override
    protected PathNavigation createNavigation(Level world) {
        return new BbGroundNavigator(this);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(3, new Raider.HoldGroundAttackGoal(this, 6.0F));
        goalSelector.addGoal(3, new RangedCrossbowAttackGoal<>(this, 0.8, 6.0F));
        goalSelector.addGoal(2, new DestroyCropGoal(this));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 15.0F, 0.02F));

        targetSelector.addGoal(1, new BbTargetPlayerGoal(this));
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor worldIn, DifficultyInstance difficultyIn, MobSpawnType reason, @Nullable SpawnGroupData spawnDataIn) {
        SpawnGroupData data = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
        setPatrolLeader(false); // Make sure that the pillagers aren't raid leaders
        setPatrolling(false);
        setCanJoinRaid(false);
        setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY.copy());
        setDropChance(EquipmentSlot.HEAD, 0);
        return data;
    }

    @Override
    protected Vec3 maybeBackOffFromEdge(Vec3 offset, MoverType mover) {
        return mobBrain.getPlotWalls().collide(getBoundingBox(), offset);
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
        return plot;
    }

    @Override
    public int meleeDamage(RandomSource random) {
        return 2 + BbMobEntity.super.meleeDamage(random);
    }

    @Override
    public float aiSpeed() {
        return 0.8f;
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.is(DamageTypes.PLAYER_ATTACK)) {
            pAmount /= 1.5f;
        }

        return super.hurt(pSource, pAmount);
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public void updateSwimming() {
        // Just use the default navigator, we never need to swim
    }

    @Override
    public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> fluid, double scale) {
        if (fluid == FluidTags.WATER) {
            return false;
        }
        return super.updateFluidHeightAndDoFluidPushing(fluid, scale);
    }

    @Override
    public boolean isEyeInFluid(TagKey<Fluid> fluid) {
        if (fluid == FluidTags.WATER) {
            return false;
        }
        return super.isEyeInFluid(fluid);
    }
}
