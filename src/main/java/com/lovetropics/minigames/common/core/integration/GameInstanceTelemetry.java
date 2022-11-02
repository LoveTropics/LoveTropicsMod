package com.lovetropics.minigames.common.core.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.integration.game_actions.GameActionHandler;
import com.lovetropics.minigames.common.core.integration.game_actions.GameActionRequest;
import com.lovetropics.minigames.common.core.integration.game_actions.GameActionType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.util.UUID;

public final class GameInstanceTelemetry implements IGameState {
	public static final GameStateKey<GameInstanceTelemetry> KEY = GameStateKey.create("Game Telemetry");

	private final IGamePhase game;
	private final Telemetry telemetry;

	private final PlayerKey initiator;

	private final GameActionHandler actions;

	private boolean closed;

	public GameInstanceTelemetry(IGamePhase game, Telemetry telemetry) {
		this.game = game;
		this.telemetry = telemetry;
		this.initiator = game.getInitiator();

		this.actions = new GameActionHandler(this.game, this);
	}

	public UUID getUuid() {
		return game.getUuid();
	}

	public void start(EventRegistrar events) {
		JsonObject payload = new JsonObject();
		payload.add("initiator", initiator.serializeProfile());
		payload.add("participants", serializeParticipantsArray());
		postImportant(ConfigLT.TELEMETRY.minigameStartEndpoint.get(), payload);

		events.listen(GamePlayerEvents.JOIN, (p) -> sendParticipantsList());
		events.listen(GamePlayerEvents.LEAVE, (p) -> sendParticipantsList());
		
		events.listen(GamePhaseEvents.START, this::requestQueuedActions);
	}

	public void finish(GameStatistics statistics) {
		JsonObject payload = new JsonObject();
		payload.addProperty("finish_time_utc", Instant.now().getEpochSecond());
		payload.add("statistics", statistics.serialize());
		payload.add("participants", serializeParticipantsArray());

		postImportant(ConfigLT.TELEMETRY.minigameEndEndpoint.get(), payload);

		close();
	}

	public void cancel() {
		postImportant(ConfigLT.TELEMETRY.minigameCancelEndpoint.get(), new JsonObject());
		close();
	}

	public void acknowledgeActionDelivery(final GameActionRequest request) {
		final JsonObject object = new JsonObject();
		object.addProperty("request", request.type().getId());
		object.addProperty("uuid", request.uuid().toString());

		telemetry.postAndRetry(ConfigLT.TELEMETRY.actionResolvedEndpoint.get(), object);
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
		for (ServerPlayer participant : game.getParticipants()) {
			participantsArray.add(PlayerKey.from(participant).serializeProfile());
		}
		return participantsArray;
	}

	private void requestQueuedActions() {
		post("/minigame/pendingactions", new JsonObject());
	}

	private void post(String endpoint, JsonObject payload) {
		post(endpoint, payload, false);
	}

	private void postImportant(String endpoint, JsonObject payload) {
		post(endpoint, payload, true);
	}

	private void post(String endpoint, JsonObject payload, boolean important) {
		if (closed) {
			return;
		}

		payload.addProperty("id", game.getUuid().toString());

		IGameDefinition definition = game.getDefinition();
		JsonObject game = new JsonObject();
		game.addProperty("id", definition.getBackendId().toString());
		game.addProperty("telemetry_key", definition.getStatisticsKey());
		game.addProperty("name", definition.getName().getString());
		payload.add("minigame", game);

		if (important) {
			telemetry.postAndRetry(endpoint, payload);
		} else {
			telemetry.post(endpoint, payload);
		}
	}

	private void close() {
		closed = true;
		telemetry.closeInstance(this);
	}

	void tick(MinecraftServer server) {
		actions.pollGameActions(server, server.getTickCount());
	}

	void handlePayload(JsonObject object, String type, String crud) {
		if ("poll".equals(type)) {
			game.invoker(GamePackageEvents.RECEIVE_POLL_EVENT).onReceivePollEvent(object, crud);
		} else if ("create".equals(crud)) {
			GameActionType.getFromId(type).ifPresent(actionType -> {
				JsonObject payload = object.getAsJsonObject("payload");
				DataResult<GameActionRequest> parseResult = actionType.getCodec().parse(JsonOps.INSTANCE, payload);

				parseResult.result().ifPresent(actions::enqueue);

				parseResult.error().ifPresent(error -> {
					LoveTropics.LOGGER.warn("Received invalid game action of type {}: {}", type, error);
				});
			});
		}
	}
}
