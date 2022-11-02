package com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

public final class BbTargetPlayerGoal extends NearestAttackableTargetGoal<Player> {
	private static final double TARGET_RANGE = 7.0;

	public BbTargetPlayerGoal(BbMobEntity owner) {
		super(owner.asMob(), Player.class, 10, true, true, entityInPlot(owner));
	}

	@Override
	protected double getFollowDistance() {
		return TARGET_RANGE;
	}

	private static Predicate<LivingEntity> entityInPlot(BbMobEntity owner) {
		return entity -> owner.getPlot().walls.containsEntity(entity);
	}
}
