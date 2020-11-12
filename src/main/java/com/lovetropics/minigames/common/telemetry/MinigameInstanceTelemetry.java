package com.lovetropics.minigames.common.telemetry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.game_actions.GameAction;
import com.lovetropics.minigames.common.game_actions.GameActionHandler;
import com.lovetropics.minigames.common.game_actions.GameActionType;
import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.PlayerSet;
import com.lovetropics.minigames.common.minigames.behaviours.MinigameBehaviorTypes;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.UUID;

public final class MinigameInstanceTelemetry implements PlayerSet.Listeners {
	private final IMinigameInstance minigame;
	private final Telemetry telemetry;

	private final UUID instanceId;
	private final IMinigameDefinition definition;
	private final PlayerKey initiator;

	private final GameActionHandler actions;

	private PlayerSet participants;
	private boolean closed;

	private MinigameInstanceTelemetry(IMinigameInstance minigame, Telemetry telemetry, UUID instanceId) {
		this.minigame = minigame;
		this.telemetry = telemetry;
		this.instanceId = instanceId;
		this.definition = minigame.getDefinition();
		this.initiator = minigame.getInitiator();

		this.telemetry.openInstance(this);
		this.actions = new GameActionHandler(this.minigame, this);
	}

	static MinigameInstanceTelemetry open(IMinigameInstance minigame, Telemetry telemetry) {
		UUID instanceId = UUID.randomUUID();
		return new MinigameInstanceTelemetry(minigame, telemetry, instanceId);
	}

	public void start(PlayerSet participants) {
		this.participants = participants;

		JsonObject payload = new JsonObject();

		payload.add("initiator", initiator.serializeProfile());

		payload.add("participants", serializeParticipantsArray());

		participants.addListener(this);

		post(ConfigLT.TELEMETRY.minigameStartEndpoint.get(), payload);
	}

	public void finish(MinigameStatistics statistics) {
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

	@Override
	public void onAddPlayer(ServerPlayerEntity player) {
		sendParticipantsList();
	}

	@Override
	public void onRemovePlayer(UUID id, @Nullable ServerPlayerEntity player) {
		sendParticipantsList();
	}

	private void sendParticipantsList() {
		JsonObject payload = new JsonObject();
		payload.add("participants", serializeParticipantsArray());

		post(ConfigLT.TELEMETRY.minigamePlayerUpdateEndpoint.get(), payload);
	}

	private JsonArray serializeParticipantsArray() {
		JsonArray participantsArray = new JsonArray();
		for (ServerPlayerEntity participant : participants) {
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
		minigame.addProperty("id", definition.getID().toString());
		minigame.addProperty("telemetry_key", definition.getTelemetryKey());
		minigame.addProperty("name", definition.getName().getString());
		payload.add("minigame", minigame);

		telemetry.post(endpoint, payload);
	}

	private void close() {
		closed = true;
		participants.removeListener(this);

		telemetry.closeInstance(this);
	}

	void tick(MinecraftServer server) {
		actions.pollGameActions(server, server.getTickCounter());
	}

	void handlePayload(JsonObject object, String type, String crud) {
		if ("poll".equals(type)) {
			IMinigameInstance active = MinigameManager.getInstance().getActiveMinigame();
			if (active.getDefinition() == definition) {
				active.getBehaviors(MinigameBehaviorTypes.POLL_FINALISTS.get()).forEach(b -> b.handlePollEvent(active.getServer(), object, crud));
			}
		} else if ("create".equals(crud)) {
			GameActionType.getFromId(type).ifPresent(request -> {
				GameAction action = request.createAction(object.getAsJsonObject("payload"));
				actions.enqueue(request, action);
			});
		}
	}
}
