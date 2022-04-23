package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.*;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.monster.HuskEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BbHuskEntity extends HuskEntity implements BbMobEntity {
	private final BbMobBrain mobBrain;
	private final Plot plot;

	public BbHuskEntity(EntityType<? extends HuskEntity> type, World world, Plot plot) {
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
	protected void addBehaviourGoals() {
		this.goalSelector.addGoal(1, new MoveToPumpkinGoal(this));
		this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
		this.goalSelector.addGoal(3, new DestroyCropGoal(this));

		this.targetSelector.addGoal(1, new BbTargetPlayerGoal(this));
	}

	@Override
	protected Vector3d maybeBackOffFromEdge(Vector3d offset, MoverType mover) {
		return mobBrain.getPlotWalls().collide(this.getBoundingBox(), offset);
	}

	@Nullable
	@Override
	public ILivingEntityData finalizeSpawn(IServerWorld world, DifficultyInstance difficulty, SpawnReason reason, @Nullable ILivingEntityData spawnData, @Nullable CompoundNBT dataTag) {
		this.setLeftHanded(this.random.nextFloat() < 0.05F);
		return spawnData;
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
