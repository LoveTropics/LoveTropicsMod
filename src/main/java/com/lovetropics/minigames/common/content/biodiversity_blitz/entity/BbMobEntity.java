package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbMobBrain;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MobEntity;

import java.util.function.Predicate;

public interface BbMobEntity {
	Predicate<MobEntity> PREDICATE = entity -> entity.isAlive() && matches(entity);

	static boolean matches(Entity entity) {
		return entity instanceof BbMobEntity;
	}

	BbMobBrain getMobBrain();
}
