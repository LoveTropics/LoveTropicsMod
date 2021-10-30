package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbGroundNavigator;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbMobBrain;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.DestroyCropGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.MoveToPumpkinGoal;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.HurtByTargetGoal;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomWalkingGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.monster.HuskEntity;
import net.minecraft.entity.monster.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
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
		this.setPathPriority(PathNodeType.DANGER_OTHER, 0.0F);
		this.setPathPriority(PathNodeType.DAMAGE_OTHER, 0.0F);
		this.setPathPriority(PathNodeType.WATER, -1.0F);

		this.getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(8);
	}

	@Override
	protected PathNavigator createNavigator(World world) {
		return new BbGroundNavigator(this);
	}

	@Override
	protected void applyEntityAI() {
		this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0D, false));
		this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D));
		this.goalSelector.addGoal(1, new MoveToPumpkinGoal(this));
		this.goalSelector.addGoal(2, new DestroyCropGoal(this));
		this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setCallsForHelp(ZombifiedPiglinEntity.class));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolemEntity.class, true));
	}

	@Override
	protected Vector3d maybeBackOffFromEdge(Vector3d offset, MoverType mover) {
		return mobBrain.getPlotWalls().collide(this.getBoundingBox(), offset);
	}

	@Nullable
	@Override
	public ILivingEntityData onInitialSpawn(IServerWorld worldIn, DifficultyInstance difficultyIn, SpawnReason reason, @Nullable ILivingEntityData spawnDataIn, @Nullable CompoundNBT dataTag) {
		if (this.rand.nextFloat() < 0.05F) {
			this.setLeftHanded(true);
		} else {
			this.setLeftHanded(false);
		}

		return spawnDataIn;
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
