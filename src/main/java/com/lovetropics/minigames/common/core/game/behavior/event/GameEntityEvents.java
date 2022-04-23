
package com.lovetropics.minigames.common.core.game.behavior.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionResult;

public final class GameEntityEvents {
	public static final GameEventType<Mounted> MOUNTED = GameEventType.create(Mounted.class, listeners -> (entityMounting, entityBeingMounted) -> {
		for (Mounted listener : listeners) {
			InteractionResult result = listener.onEntityMounted(entityMounting, entityBeingMounted);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});

	private GameEntityEvents() {
	}

	public interface Mounted {
		InteractionResult onEntityMounted(Entity entityMounting, Entity entityBeingMounted);
	}
}
