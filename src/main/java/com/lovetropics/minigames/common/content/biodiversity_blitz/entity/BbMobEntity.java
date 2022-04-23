package com.lovetropics.minigames.common.content.biodiversity_blitz.entity;

import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.ai.BbMobBrain;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.function.Predicate;

public interface BbMobEntity {
	Predicate<Mob> PREDICATE = entity -> entity.isAlive() && matches(entity);

	static boolean matches(Entity entity) {
		return entity instanceof BbMobEntity;
	}

	BbMobBrain getMobBrain();

	Mob asMob();

	Plot getPlot();
}
