package com.lovetropics.minigames.common.core.integration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.core.game.IGameDefinition;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.DonationPackageData;
import com.lovetropics.minigames.common.core.game.state.GamePackageState;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.integration.game_actions.GameActionHandler;
import com.lovetropics.minigames.common.core.integration.game_actions.GameActionRequest;
import com.lovetropics.minigames.common.core.integration.game_actions.GameActionType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class GameInstanceIntegrations implements IGameState {
	public static final GameStateKey<GameInstanceIntegrations> KEY = GameStateKey.create("Game Integrations");

	private static final Codec<List<DonationPackageData>> PACKAGES_CODEC = DonationPackageData.Payload.CODEC.codec()
			.xmap(DonationPackageData.Payload::data, DonationPackageData::asPayload)
			.listOf();

	private final IGamePhase game;
	private final BackendIntegrations integrations;

	private final PlayerKey initiator;

	private final GameActionHandler actions;

	private boolean closed;

	public GameInstanceIntegrations(IGamePhase game, BackendIntegrations integrations) {
		this.game = game;
		this.integrations = integrations;
		initiator = game.initiator();

		actions = new GameActionHandler(this.game, this);
	}

	public UUID getUuid() {
		return game.gameUuid();
	}

	public void start(EventRegistrar events) {
		JsonObject payload = new JsonObject();
		payload.add("initiator", initiator.serializeProfile());
		payload.add("participants", serializeParticipantsArray());
		addGameDefinitionData(payload);

		postImportant(ConfigLT.INTEGRATIONS.minigameStartEndpoint.get(), payload);

		events.listen(GamePlayerEvents.LEAVE, (p) -> sendParticipantsList());
		events.listen(GamePlayerEvents.SET_ROLE, (p, r, lr) -> sendParticipantsList());

		requestQueuedActions();
	}

	private void addGameDefinitionData(JsonObject payload) {
		IGameDefinition definition = game.definition();
		payload.add("name", ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, definition.name()).getOrThrow());
		Component subtitle = definition.subtitle();
		if (subtitle != null) {
			payload.add("subtitle", ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, subtitle).getOrThrow());
		}

		GamePackageState packageState = game.state().getOrNull(GamePackageState.KEY);
		if (packageState != null) {
			List<DonationPackageData> packageList = List.copyOf(packageState.packages());
			payload.add("packages", PACKAGES_CODEC.encodeStart(JsonOps.INSTANCE, packageList).getOrThrow());
		}
	}

	public void finish(GameStatistics statistics) {
		JsonObject payload = new JsonObject();
		payload.addProperty("finish_time_utc", Instant.now().getEpochSecond());
		payload.add("statistics", statistics.serialize());
		payload.add("participants", serializeParticipantsArray());

		postImportant(ConfigLT.INTEGRATIONS.minigameEndEndpoint.get(), payload);

		close();
	}

	public void cancel() {
		postImportant(ConfigLT.INTEGRATIONS.minigameCancelEndpoint.get(), new JsonObject());
		close();
	}

	public void acknowledgeActionDelivery(final GameActionRequest request) {
		final JsonObject object = new JsonObject();
		object.addProperty("request", request.type().getId());
		object.addProperty("uuid", request.uuid().toString());

		integrations.postAndRetry(ConfigLT.INTEGRATIONS.actionResolvedEndpoint.get(), object);
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
		integrations.postPolling(ConfigLT.INTEGRATIONS.addPollEndpoint.get(), object);
	}

	private void sendParticipantsList() {
		JsonObject payload = new JsonObject();
		payload.add("participants", serializeParticipantsArray());

		post(ConfigLT.INTEGRATIONS.minigamePlayerUpdateEndpoint.get(), payload);
	}

	private JsonArray serializeParticipantsArray() {
		JsonArray participantsArray = new JsonArray();
		for (ServerPlayer participant : game.participants()) {
			participantsArray.add(PlayerKey.from(participant).serializeProfile());
		}
		return participantsArray;
	}

	private void requestQueuedActions() {
		post(ConfigLT.INTEGRATIONS.pendingActionsEndpoint.get(), new JsonObject());
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

		payload.addProperty("id", game.gameUuid().toString());

		IGameDefinition definition = game.definition();
		JsonObject game = new JsonObject();
		game.addProperty("id", definition.backendId().toString());
		game.addProperty("telemetry_key", definition.statisticsKey());
		game.addProperty("name", definition.name().getString());
		payload.add("minigame", game);

		if (important) {
			integrations.postAndRetry(endpoint, payload);
		} else {
			integrations.post(endpoint, payload);
		}
	}

	private void close() {
		closed = true;
		integrations.closeInstance(this);
	}

	void tick(MinecraftServer server) {
		actions.pollGameActions(server.getTickCount());
	}

	void handlePayload(JsonObject object, String type, Crud crud) {
		if ("poll".equals(type)) {
			game.invoker(GamePackageEvents.RECEIVE_POLL_EVENT).onReceivePollEvent(object, crud);
		} else if (crud == Crud.CREATE) {
			Optional<GameActionType> actionType = GameActionType.getFromId(type);
			if (actionType.isPresent()) {
				// TODO: Fallback because format is inconsistent
				JsonObject payload = object.has("payload") ? object.getAsJsonObject("payload") : object;
				DataResult<GameActionRequest> parseResult = actionType.get().getCodec().parse(JsonOps.INSTANCE, payload);

				parseResult.result().ifPresent(actions::enqueue);
				parseResult.error().ifPresent(error -> LoveTropics.LOGGER.warn("Received invalid game action of type {}: {}", type, error));
			} else {
				LoveTropics.LOGGER.debug("Received create event with unrecognised action type: {}", type);
			}
		}
	}
}
