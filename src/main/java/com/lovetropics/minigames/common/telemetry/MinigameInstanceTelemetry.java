package com.lovetropics.minigames.common.telemetry;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.PlayerSet;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.UUID;

public final class MinigameInstanceTelemetry implements PlayerSet.Listeners {
	private final Telemetry telemetry;

	private final UUID instanceId;
	private final IMinigameDefinition definition;
	private final PlayerKey initiator;

	private PlayerSet participants;
	private boolean closed;

	private MinigameInstanceTelemetry(Telemetry telemetry, UUID instanceId, IMinigameDefinition definition, PlayerKey initiator) {
		this.telemetry = telemetry;
		this.instanceId = instanceId;
		this.definition = definition;
		this.initiator = initiator;
	}

	static MinigameInstanceTelemetry open(Telemetry telemetry, IMinigameDefinition definition, PlayerKey initiator) {
		UUID instanceId = UUID.randomUUID();
		return new MinigameInstanceTelemetry(telemetry, instanceId, definition, initiator);
	}

	public void start(PlayerSet participants) {
		this.participants = participants;

		JsonObject payload = new JsonObject();

		JsonObject minigame = new JsonObject();
		minigame.addProperty("id", definition.getTelemetryKey());
		minigame.addProperty("name", definition.getName().getString());
		payload.add("minigame", minigame);

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

		closed = true;
		participants.removeListener(this);
	}

	public void cancel() {
		post(ConfigLT.TELEMETRY.minigameCancelEndpoint.get(), new JsonObject());

		participants.removeListener(this);
		closed = true;
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
		telemetry.post(endpoint, payload);
	}
}
