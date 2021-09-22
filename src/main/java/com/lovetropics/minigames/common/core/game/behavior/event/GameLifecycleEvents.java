package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IActiveGame;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.List;

public final class GameLifecycleEvents {
	public static final GameEventType<Start> START = GameEventType.create(Start.class, listeners -> game -> {
		for (Start listener : listeners) {
			listener.start(game);
		}
	});

	public static final GameEventType<Stop> STOP = GameEventType.create(Stop.class, listeners -> (game, reason) -> {
		for (Stop listener : listeners) {
			listener.stop(game, reason);
		}
	});

	public static final GameEventType<Stop> POST_STOP = GameEventType.create(Stop.class, listeners -> (game, reason) -> {
		for (Stop listener : listeners) {
			listener.stop(game, reason);
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
		void start(IActiveGame game);
	}

	public interface Stop {
		void stop(IActiveGame game, GameStopReason reason);
	}

	public interface Tick {
		void tick(IActiveGame game);
	}

	public interface AssignRoles {
		void assignRoles(IActiveGame game, List<ServerPlayerEntity> participants, List<ServerPlayerEntity> spectators);
	}
}
