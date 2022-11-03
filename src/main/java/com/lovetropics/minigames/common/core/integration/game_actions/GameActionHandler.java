package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.integration.GameInstanceTelemetry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public final class GameActionHandler {
	private static final Logger LOGGER = LogManager.getLogger(GameActionHandler.class);

	private final IGamePhase game;
	private final GameInstanceTelemetry telemetry;
	private final Map<GameActionType, ActionsQueue> queues = new EnumMap<>(GameActionType.class);

	public GameActionHandler(IGamePhase game, GameInstanceTelemetry telemetry) {
		this.telemetry = telemetry;
		this.game = game;
	}

	public void pollGameActions(final int tick) {
		for (ActionsQueue queue : queues.values()) {
			try {
				GameActionRequest request = queue.tryHandle(game, tick);
				if (request != null) {
					// If we resolved the action, send acknowledgement to the backend
					telemetry.acknowledgeActionDelivery(request);
				}
			} catch (Exception e) {
				LOGGER.error("Failed to resolve game action", e);
			}
		}
	}

	public void enqueue(final GameActionRequest request) {
		ActionsQueue queue = getQueueFor(request.type());
		queue.offer(request);
	}

	private ActionsQueue getQueueFor(GameActionType requestType) {
		return queues.computeIfAbsent(requestType, ActionsQueue::new);
	}

	static class ActionsQueue {
		private final GameActionType requestType;
		private final Queue<GameActionRequest> queue = new ConcurrentLinkedDeque<>();
		private int nextPollTick;

		ActionsQueue(GameActionType requestType) {
			this.requestType = requestType;
		}

		@Nullable
		public GameActionRequest tryHandle(IGamePhase game, int tick) {
			if (tick >= nextPollTick) {
				GameActionRequest request = queue.poll();
				nextPollTick = tick + requestType.getPollingIntervalTicks();
				if (request != null) {
					if (request.action().resolve(game, game.getServer())) {
						return request;
					} else {
						queue.offer(request);
					}
				}
			}
			return null;
		}

		public void offer(GameActionRequest request) {
			if (!queue.contains(request)) {
				queue.offer(request);
			}
		}
	}
}
