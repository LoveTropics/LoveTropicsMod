package com.lovetropics.minigames.common.core.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.lobby.GameLobbyId;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.integration.game_actions.GameAction;
import com.lovetropics.minigames.common.core.integration.game_actions.GameActionHandler;
import com.lovetropics.minigames.common.core.integration.game_actions.GameActionType;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.time.Instant;

// TODO: update backend for handling lobby system: should it be local to a lobby or to an actual minigame?
public final class GameInstanceTelemetry implements IGameState {
	public static final GameStateKey<GameInstanceTelemetry> KEY = GameStateKey.create("Game Telemetry");

	private final IGamePhase game;
	private final Telemetry telemetry;

	private final PlayerKey initiator;

	private final GameActionHandler actions;

	private boolean closed;

	private GameInstanceTelemetry(IGamePhase game, Telemetry telemetry) {
		this.game = game;
		this.telemetry = telemetry;
		this.initiator = game.getInitiator();

		this.telemetry.openInstance(this);
		this.actions = new GameActionHandler(this.game, this);
	}

	static GameInstanceTelemetry open(IGamePhase game, Telemetry telemetry) {
		return new GameInstanceTelemetry(game, telemetry);
	}

	// TODO: game specific id?
	public GameLobbyId getLobbyId() {
		return game.getLobby().getMetadata().id();
	}

	public void start(EventRegistrar events) {
		JsonObject payload = new JsonObject();
		payload.add("initiator", initiator.serializeProfile());
		payload.add("participants", serializeParticipantsArray());
		post(ConfigLT.TELEMETRY.minigameStartEndpoint.get(), payload);

		events.listen(GamePlayerEvents.JOIN, (p) -> sendParticipantsList());
		events.listen(GamePlayerEvents.LEAVE, (p) -> sendParticipantsList());
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

		payload.addProperty("id", game.getLobby().getMetadata().id().uuid().toString());

		// TODO
		/*JsonObject minigame = new JsonObject();
		minigame.addProperty("id", definition.getDisplayId().toString());
		minigame.addProperty("telemetry_key", definition.getTelemetryKey());
		minigame.addProperty("name", definition.getName().getString());
		payload.add("minigame", minigame);*/

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
			game.invoker(GamePackageEvents.RECEIVE_POLL_EVENT).onReceivePollEvent(object, crud);
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
