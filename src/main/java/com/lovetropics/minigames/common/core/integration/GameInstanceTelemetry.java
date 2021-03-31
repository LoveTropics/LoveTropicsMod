package com.lovetropics.minigames.common.core.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.core.game.SingleGameManager;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.integration.game_actions.GameAction;
import com.lovetropics.minigames.common.core.integration.game_actions.GameActionHandler;
import com.lovetropics.minigames.common.core.integration.game_actions.GameActionType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.time.Instant;
import java.util.UUID;

public final class GameInstanceTelemetry {
	private final IGameInstance game;
	private final Telemetry telemetry;

	private final UUID instanceId;
	private final IGameDefinition definition;
	private final PlayerKey initiator;

	private final GameActionHandler actions;

	private boolean closed;

	private GameInstanceTelemetry(IGameInstance game, Telemetry telemetry, UUID instanceId) {
		this.game = game;
		this.telemetry = telemetry;
		this.instanceId = instanceId;
		this.definition = game.getDefinition();
		this.initiator = game.getInitiator();

		this.telemetry.openInstance(this);
		this.actions = new GameActionHandler(this.game, this);
	}

	static GameInstanceTelemetry open(IGameInstance game, Telemetry telemetry) {
		UUID instanceId = UUID.randomUUID();
		return new GameInstanceTelemetry(game, telemetry, instanceId);
	}

	public void start() {
		JsonObject payload = new JsonObject();
		payload.add("initiator", initiator.serializeProfile());
		payload.add("participants", serializeParticipantsArray());
		post(ConfigLT.TELEMETRY.minigameStartEndpoint.get(), payload);

		GameEventListeners events = game.getEvents();
		events.listen(GamePlayerEvents.JOIN, (g, p, r) -> sendParticipantsList());
		events.listen(GamePlayerEvents.LEAVE, (g, p) -> sendParticipantsList());
	}

	public void finish(GameStatistics statistics) {
		long finishTime = System.currentTimeMillis() / 1000;

		JsonObject payload = new JsonObject();
		payload.addProperty("finish_time_utc", finishTime);
		payload.add("statistics", statistics.serialize());

		post(ConfigLT.TELEMETRY.minigameEndEndpoint.get(), payload);

		close();
	}

	public void cancel() {
		post(ConfigLT.TELEMETRY.minigameCancelEndpoint.get(), new JsonObject());
		close();
	}

	public void acknowledgeActionDelivery(final GameActionType request, final GameAction action) {
		final JsonObject object = new JsonObject();
		object.addProperty("request", request.getId());
		object.addProperty("uuid", action.uuid.toString());

		telemetry.post(ConfigLT.TELEMETRY.actionResolvedEndpoint.get(), object);
	}

	public void createPoll(String title, String duration, String... options) {
		if (options.length < 2) {
			throw new IllegalArgumentException("Poll must have more than 1 choice");
		}
		JsonObject object = new JsonObject();
		object.addProperty("title", title);
		object.addProperty("start", Instant.now().getEpochSecond());
		object.addProperty("duration", duration);
		JsonArray array = new JsonArray();
		for (String option : options) {
			array.add(option);
		}
		object.add("options", array);
		telemetry.postPolling("polls/add", object);
	}

	private void sendParticipantsList() {
		JsonObject payload = new JsonObject();
		payload.add("participants", serializeParticipantsArray());

		post(ConfigLT.TELEMETRY.minigamePlayerUpdateEndpoint.get(), payload);
	}

	private JsonArray serializeParticipantsArray() {
		JsonArray participantsArray = new JsonArray();
		for (ServerPlayerEntity participant : game.getParticipants()) {
			participantsArray.add(PlayerKey.from(participant).serializeProfile());
		}
		return participantsArray;
	}

	private void post(String endpoint, JsonObject payload) {
		if (closed) {
			return;
		}

		payload.addProperty("id", instanceId.toString());

		JsonObject minigame = new JsonObject();
		minigame.addProperty("id", definition.getDisplayId().toString());
		minigame.addProperty("telemetry_key", definition.getTelemetryKey());
		minigame.addProperty("name", definition.getName().getString());
		payload.add("minigame", minigame);

		telemetry.post(endpoint, payload);
	}

	private void close() {
		closed = true;
		telemetry.closeInstance(this);
	}

	void tick(MinecraftServer server) {
		actions.pollGameActions(server, server.getTickCounter());
	}

	void handlePayload(JsonObject object, String type, String crud) {
		if ("poll".equals(type)) {
			IGameInstance active = SingleGameManager.INSTANCE.getActiveGame();
			if (active.getDefinition() == definition) {
				active.invoker(GamePackageEvents.RECEIVE_POLL_EVENT).onReceivePollEvent(game, object, crud);
			}
		} else if ("create".equals(crud)) {
			GameActionType.getFromId(type).ifPresent(actionType -> {
				JsonObject payload = object.getAsJsonObject("payload");
				DataResult<? extends GameAction> parseResult = actionType.getCodec().parse(JsonOps.INSTANCE, payload);

				parseResult.result().ifPresent(action -> actions.enqueue(actionType, action));

				parseResult.error().ifPresent(error -> {
					LoveTropics.LOGGER.warn("Received invalid game action of type {}: {}", type, error);
				});
			});
		}
	}
}
