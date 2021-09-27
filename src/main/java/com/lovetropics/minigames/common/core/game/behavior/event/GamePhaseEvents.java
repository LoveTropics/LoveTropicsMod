package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.GameStopReason;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.List;

public final class GamePhaseEvents {
	public static final GameEventType<Start> INITIALIZE = GameEventType.create(Start.class, listeners -> () -> {
		for (Start listener : listeners) {
			listener.start();
		}
	});

	public static final GameEventType<Start> START = GameEventType.create(Start.class, listeners -> () -> {
		for (Start listener : listeners) {
			listener.start();
		}
	});

	public static final GameEventType<Stop> STOP = GameEventType.create(Stop.class, listeners -> (reason) -> {
		for (Stop listener : listeners) {
			listener.stop(reason);
		}
	});

	public static final GameEventType<Tick> TICK = GameEventType.create(Tick.class, listeners -> () -> {
		for (Tick listener : listeners) {
			listener.tick();
		}
	});

	private GamePhaseEvents() {
	}

	public interface Start {
		void start();
	}

	public interface Stop {
		void stop(GameStopReason reason);
	}

	public interface Tick {
		void tick();
	}

	public interface AssignRoles {
		void assignRoles(List<ServerPlayerEntity> participants, List<ServerPlayerEntity> spectators);
	}
}
