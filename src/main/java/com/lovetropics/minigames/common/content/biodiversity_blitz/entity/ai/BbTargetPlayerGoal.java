package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Predicate;

public final class BbTargetPlayerGoal extends NearestAttackableTargetGoal<PlayerEntity> {
	private static final double TARGET_RANGE = 8.0;

	public BbTargetPlayerGoal(BbMobEntity owner) {
		super(owner.asMob(), PlayerEntity.class, 10, true, true, entityInPlot(owner));
	}

	@Override
	protected double getTargetDistance() {
		return TARGET_RANGE;
	}

	private static Predicate<LivingEntity> entityInPlot(BbMobEntity owner) {
		return entity -> owner.getPlot().walls.containsEntity(entity);
	}
}
