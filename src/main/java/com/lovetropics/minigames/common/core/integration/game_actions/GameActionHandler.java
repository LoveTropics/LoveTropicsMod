package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.integration.GameInstanceIntegrations;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.PriorityBlockingQueue;

public final class GameActionHandler {
	private static final Logger LOGGER = LogUtils.getLogger();

	private final GameInstanceIntegrations integrations;
	private final Map<GameActionType, ActionsQueue> queues = new EnumMap<>(GameActionType.class);

	public GameActionHandler(GameInstanceIntegrations integrations) {
		this.integrations = integrations;
	}

	public void pollGameActions(IGamePhase game, int tick) {
		for (ActionsQueue queue : queues.values()) {
			GameActionRequest request = queue.tryHandle(game, tick);
			if (request != null && request.type().sendsAcknowledgement()) {
				// If we resolved the action, send acknowledgement to the backend
				integrations.acknowledgeActionDelivery(request);
			}
		}
	}

	public void enqueue(final GameActionRequest request) {
		ActionsQueue queue = getQueueFor(request.type());
		queue.offer(request);
		LOGGER.debug("Enqueued incoming game action request: {}", request);
	}

	private ActionsQueue getQueueFor(GameActionType requestType) {
		return queues.computeIfAbsent(requestType, ActionsQueue::new);
	}

	static class ActionsQueue {
		private final GameActionType requestType;
		private final Queue<GameActionRequest> queue = new PriorityBlockingQueue<>(1, Comparator.comparing(GameActionRequest::time));
		private final Queue<GameActionRequest> deferredQueue = new ConcurrentLinkedDeque<>();
		private int nextPollTick;

		ActionsQueue(GameActionType requestType) {
			this.requestType = requestType;
		}

		@Nullable
		public GameActionRequest tryHandle(IGamePhase game, int tick) {
			if (queue.isEmpty() && deferredQueue.isEmpty() || tick < nextPollTick) {
				return null;
			}
			nextPollTick = tick + requestType.getPollingIntervalTicks();
			GameActionRequest handledRequest = tryHandleQueue(game, queue);
			if (handledRequest != null) {
				return handledRequest;
			}
			return tryHandleQueue(game, deferredQueue);
		}

		@Nullable
		private GameActionRequest tryHandleQueue(IGamePhase game, Queue<GameActionRequest> queue) {
			List<GameActionRequest> unhandledRequests = new ArrayList<>();
			try {
				GameActionRequest request;
				while ((request = queue.poll()) != null) {
					LOGGER.debug("Trying to resolve incoming game action request: {}", request);
					try {
						if (request.action().resolve(game, game.server())) {
							return request;
						} else {
							unhandledRequests.add(request);
						}
					} catch (Exception e) {
						LOGGER.error("An unexpected exception occurred while resolving game action: {}", request, e);
					}
				}
				return null;
			} finally {
				deferredQueue.addAll(unhandledRequests);
			}
		}

		public void offer(GameActionRequest request) {
			if (!queue.contains(request)) {
				queue.offer(request);
			}
		}
	}
}
