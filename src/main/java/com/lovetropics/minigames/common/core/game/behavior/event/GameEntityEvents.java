
package com.lovetropics.minigames.common.core.game.behavior.event;

import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResultType;

public final class GameEntityEvents {
	public static final GameEventType<Mounted> MOUNTED = GameEventType.create(Mounted.class, listeners -> (entityMounting, entityBeingMounted) -> {
		for (Mounted listener : listeners) {
			ActionResultType result = listener.onEntityMounted(entityMounting, entityBeingMounted);
			if (result != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResultType.PASS;
	});

	private GameEntityEvents() {
	}

	public interface Mounted {
		ActionResultType onEntityMounted(Entity entityMounting, Entity entityBeingMounted);
	}
}
