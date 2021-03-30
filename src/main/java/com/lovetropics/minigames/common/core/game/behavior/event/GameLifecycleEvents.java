package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.List;

public final class GameLifecycleEvents {
	public static final GameEventType<Start> START = GameEventType.create(Start.class, listeners -> game -> {
		for (Start listener : listeners) {
			listener.start(game);
		}
	});

	public static final GameEventType<Stop> FINISH = GameEventType.create(Stop.class, listeners -> game -> {
		for (Stop listener : listeners) {
			listener.stop(game);
		}
	});

	public static final GameEventType<Stop> POST_FINISH = GameEventType.create(Stop.class, listeners -> game -> {
		for (Stop listener : listeners) {
			listener.stop(game);
		}
	});

	public static final GameEventType<Stop> CANCEL = GameEventType.create(Stop.class, listeners -> game -> {
		for (Stop listener : listeners) {
			listener.stop(game);
		}
	});

	public static final GameEventType<Tick> TICK = GameEventType.create(Tick.class, listeners -> game -> {
		for (Tick listener : listeners) {
			listener.tick(game);
		}
	});

	public static final GameEventType<AssignRoles> ASSIGN_ROLES = GameEventType.create(AssignRoles.class, listeners -> (game, participants, spectators) -> {
		for (AssignRoles listener : listeners) {
			listener.assignRoles(game, participants, spectators);
		}
	});

	private GameLifecycleEvents() {
	}

	public interface Start {
		void start(IGameInstance game);
	}

	public interface Stop {
		void stop(IGameInstance game);
	}

	public interface Tick {
		void tick(IGameInstance game);
	}

	public interface AssignRoles {
		void assignRoles(IGameInstance game, List<ServerPlayerEntity> participants, List<ServerPlayerEntity> spectators);
	}
}
