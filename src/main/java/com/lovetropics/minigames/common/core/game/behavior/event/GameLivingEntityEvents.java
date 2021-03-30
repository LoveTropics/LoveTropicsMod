package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import net.minecraft.entity.LivingEntity;

public final class GameLivingEntityEvents {
	public static final GameEventType<Tick> TICK = GameEventType.create(Tick.class, listeners -> (game, entity) -> {
		for (Tick listener : listeners) {
			listener.tick(game, entity);
		}
	});

	private GameLivingEntityEvents() {
	}

	public interface Tick {
		void tick(IGameInstance game, LivingEntity entity);
	}
}
