package com.lovetropics.minigames.common.core.game.behavior.event;

import com.lovetropics.minigames.common.core.game.GameStopReason;

public final class GamePhaseEvents {
	public static final GameEventType<Start> CREATE = GameEventType.create(Start.class, listeners -> () -> {
		for (Start listener : listeners) {
			listener.start();
		}
	});

	public static final GameEventType<Destroy> DESTROY = GameEventType.create(Destroy.class, listeners -> () -> {
		for (Destroy listener : listeners) {
			listener.destroy();
		}
	});

	public static final GameEventType<Start> START = GameEventType.create(Start.class, listeners -> () -> {
		for (Start listener : listeners) {
			listener.start();
		}
	});

	public static final GameEventType<Stop> STOP = GameEventType.create(Stop.class, listeners -> reason -> {
		for (Stop listener : listeners) {
			listener.stop(reason);
		}
	});

	public static final GameEventType<Finish> FINISH = GameEventType.create(Finish.class, listeners -> () -> {
		for (Finish listener : listeners) {
			listener.finish();
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

	public interface Destroy {
		void destroy();
	}

	public interface Finish {
		void finish();
	}

	public interface Tick {
		void tick();
	}
}
