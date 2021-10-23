package com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.entity.ai.MoveToPumpkinGoal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
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
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class MpHuskEntity extends HuskEntity implements MpMobEntity {
	private final ScareManager scares = new ScareManager(2);

	private final PlotWalls plotWalls;

	public MpHuskEntity(EntityType<? extends HuskEntity> type, World world, PlotWalls plotWalls) {
		super(type, world);
		this.plotWalls = plotWalls;

		// Ignore sweet berry bushes and water
		this.setPathPriority(PathNodeType.DANGER_OTHER, 0.0F);
		this.setPathPriority(PathNodeType.DAMAGE_OTHER, 0.0F);
		this.setPathPriority(PathNodeType.WATER, -1.0F);
	}

	@Override
	protected PathNavigator createNavigator(World world) {
		return new MpGroundNavigator(this);
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
	protected Vector3d maybeBackOffFromEdge(Vector3d offset, MoverType mover) {
		return plotWalls.collide(this.getBoundingBox(), offset);
	}

	@Override
	public ScareManager getScareManager() {
		return scares;
	}

	@Override
	public PlotWalls getPlotWalls() {
		return plotWalls;
	}
}
