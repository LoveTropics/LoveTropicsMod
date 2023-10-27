package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.impl;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.*;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class BbHuskEntity extends Husk implements BbMobEntity {
	private final BbMobBrain mobBrain;
	private final Plot plot;

	public BbHuskEntity(EntityType<? extends Husk> type, Level world, Plot plot) {
		super(type, world);
		this.mobBrain = new BbMobBrain(plot.walls);
		this.plot = plot;

		// Ignore sweet berry bushes and water
		this.setPathfindingMalus(BlockPathTypes.DANGER_OTHER, 0.0F);
		this.setPathfindingMalus(BlockPathTypes.DAMAGE_OTHER, 0.0F);
		this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
	}

	@Override
	protected PathNavigation createNavigation(Level world) {
		return new BbGroundNavigator(this);
	}

	@Override
	protected void addBehaviourGoals() {
		this.goalSelector.addGoal(2, new DestroyCropGoal(this));
		this.goalSelector.addGoal(3, new ZombieAttackGoal(this, 1.0, false));

		this.targetSelector.addGoal(1, new BbTargetPlayerGoal(this));
	}

	@Override
	protected Vec3 maybeBackOffFromEdge(Vec3 offset, MoverType mover) {
		return mobBrain.getPlotWalls().collide(this.getBoundingBox(), offset);
	}

	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
		this.setLeftHanded(this.random.nextFloat() < 0.05F);
		return spawnData;
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
}
