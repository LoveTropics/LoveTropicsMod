package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.integration.GameInstanceTelemetry;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.PriorityBlockingQueue;

public final class GameActionHandler {
	private static final Logger LOGGER = LogManager.getLogger(GameActionHandler.class);

	private final IGamePhase game;
	private final GameInstanceTelemetry telemetry;
	private final Map<GameActionType, ActionsQueue> queues = new EnumMap<>(GameActionType.class);

	public GameActionHandler(IGamePhase game, GameInstanceTelemetry telemetry) {
		this.telemetry = telemetry;
		this.game = game;
	}

	public void pollGameActions(final MinecraftServer server, final int tick) {
		for (final Map.Entry<GameActionType, ActionsQueue> entry : queues.entrySet()) {
			final GameActionType request = entry.getKey();
			final ActionsQueue polling = entry.getValue();

			GameAction action = polling.tryPoll(tick);
			try {
				if (action != null && action.resolve(game, server)) {
					// If we resolved the action, send acknowledgement to the backend
					telemetry.acknowledgeActionDelivery(request, action);
				}
			} catch (Exception e) {
				LOGGER.error("Failed to resolve game action", e);
			}
		}
	}

	public void enqueue(final GameActionType requestType, final GameAction action) {
		ActionsQueue queue = getQueueFor(requestType);
		queue.offer(action);
	}

	private ActionsQueue getQueueFor(GameActionType requestType) {
		return queues.computeIfAbsent(requestType, ActionsQueue::new);
	}

	static class ActionsQueue {
		private final GameActionType requestType;
		private final Queue<GameAction> queue = new PriorityBlockingQueue<>();
		private int nextPollTick;

		ActionsQueue(GameActionType requestType) {
			this.requestType = requestType;
		}

		@Nullable
		public GameAction tryPoll(int tick) {
			if (!queue.isEmpty() && tick >= nextPollTick) {
				nextPollTick = tick + requestType.getPollingIntervalTicks();
				return queue.poll();
			}

			return null;
		}

		public void offer(GameAction action) {
			if (!queue.contains(action)) {
				queue.offer(action);
			}
		}
	}
}
