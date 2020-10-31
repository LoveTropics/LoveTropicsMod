package com.lovetropics.minigames.common.telemetry;

import com.lovetropics.minigames.common.game_actions.GameAction;
import net.minecraft.server.MinecraftServer;

import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public final class GameActionHandler {
	private final MinigameInstanceTelemetry telemetry;
	private final Map<BackendRequest, ActionsQueue> queues = new EnumMap<>(BackendRequest.class);

	public GameActionHandler(MinigameInstanceTelemetry telemetry) {
		this.telemetry = telemetry;
	}

	void pollGameActions(final MinecraftServer server, final int tick) {
		for (final Map.Entry<BackendRequest, ActionsQueue> entry : queues.entrySet()) {
			final BackendRequest request = entry.getKey();
			final ActionsQueue polling = entry.getValue();

			if (!polling.isEmpty() && polling.tryPoll(tick)) {
				final GameAction action = polling.poll();
				if (action == null) {
					continue;
				}

				// If we resolved the action, send acknowledgement to the backend
				if (action.resolve(server)) {
					telemetry.acknowledgeActionDelivery(request, action);
				}
			}
		}
	}

	public void enqueue(final BackendRequest requestType, final GameAction action) {
		ActionsQueue queue = getQueueFor(requestType);
		queue.offer(action);
	}

	private ActionsQueue getQueueFor(BackendRequest requestType) {
		ActionsQueue queue = queues.get(requestType);
		if (queue == null) {
			queues.put(requestType, queue = new ActionsQueue(requestType));
		}
		return queue;
	}

	static class ActionsQueue {
		private final BackendRequest requestType;
		private int lastPolledTick = 0;
		private final Queue<GameAction> queue = new PriorityBlockingQueue<>();

		ActionsQueue(BackendRequest requestType) {
			this.requestType = requestType;
		}

		public boolean tryPoll(int tick) {
			if (tick >= lastPolledTick + (requestType.getPollingIntervalSeconds() * 20)) {
				lastPolledTick = tick;
				return true;
			}
			return false;
		}

		public void offer(GameAction action) {
			if (!queue.contains(action)) {
				queue.offer(action);
			}
		}

		public GameAction poll() {
			return queue.poll();
		}

		public boolean isEmpty() {
			return queue.isEmpty();
		}
	}
}
