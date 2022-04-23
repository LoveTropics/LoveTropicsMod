package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.*;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.goal.RangedCrossbowAttackGoal;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.monster.PillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BbPillagerEntity extends PillagerEntity implements BbMobEntity {
    private final BbMobBrain mobBrain;
    private final Plot plot;

    public BbPillagerEntity(EntityType<? extends PillagerEntity> type, World world, Plot plot) {
        super(type, world);
        this.mobBrain = new BbMobBrain(plot.walls);
        this.plot = plot;

        // Ignore sweet berry bushes and water
        this.setPathfindingMalus(PathNodeType.DANGER_OTHER, 0.0F);
        this.setPathfindingMalus(PathNodeType.DAMAGE_OTHER, 0.0F);
        this.setPathfindingMalus(PathNodeType.WATER, -1.0F);
    }

    @Override
    protected PathNavigator createNavigation(World world) {
        return new BbGroundNavigator(this);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new MoveToPumpkinGoal(this));
        this.goalSelector.addGoal(2, new AbstractRaiderEntity.FindTargetGoal(this, 6.0F));
        this.goalSelector.addGoal(2, new RangedCrossbowAttackGoal<>(this, 1.0, 6.0F));
        this.goalSelector.addGoal(3, new DestroyCropGoal(this));
        this.goalSelector.addGoal(4, new LookAtGoal(this, PlayerEntity.class, 15.0F, 0.02F));

        this.targetSelector.addGoal(1, new BbTargetPlayerGoal(this));
    }

    @Nullable
    @Override
    public ILivingEntityData finalizeSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
        ILivingEntityData data = super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn, dataTag);
        this.setPatrolLeader(false); // Make sure that the pillagers aren't raid leaders
        this.setPatrolling(false);
        this.setCanJoinRaid(false);
        this.setItemSlot(EquipmentSlotType.HEAD, ItemStack.EMPTY.copy());
        this.setDropChance(EquipmentSlotType.HEAD, 0);
        return data;
    }

    @Override
    protected Vector3d maybeBackOffFromEdge(Vector3d offset, MoverType mover) {
        return mobBrain.getPlotWalls().collide(this.getBoundingBox(), offset);
    }

    @Override
    public BbMobBrain getMobBrain() {
        return mobBrain;
    }

    @Override
    public MobEntity asMob() {
        return this;
    }

    @Override
    public Plot getPlot() {
        return this.plot;
    }
}
