package com.lovetropics.minigames.common.core.game.behavior.event;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;

public final class GameLivingEntityEvents {
	public static final GameEventType<Tick> TICK = GameEventType.create(Tick.class, listeners -> entity -> {
		for (Tick listener : listeners) {
			listener.tick(entity);
		}
	});

	public static final GameEventType<Death> DEATH = GameEventType.create(Death.class, listeners -> (entity, damageSource) -> {
		for (Death listener : listeners) {
			ActionResultType result = listener.onDeath(entity, damageSource);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.PASS;
	});

	private GameLivingEntityEvents() {
	}

	public interface Tick {
		void tick(LivingEntity entity);
	}

	public interface Death {
		ActionResultType onDeath(LivingEntity entity, DamageSource damageSource);
	}
}
