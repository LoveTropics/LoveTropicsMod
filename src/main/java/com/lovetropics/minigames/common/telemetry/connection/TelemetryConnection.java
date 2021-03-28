package com.lovetropics.minigames.common.telemetry.connection;

import com.google.gson.JsonObject;

import javax.annotation.Nullable;

public interface TelemetryConnection {
	boolean send(JsonObject payload);

	boolean isConnected();

	interface Handler {
		void acceptOpened();

		void acceptMessage(JsonObject payload);

		void acceptError(Throwable cause);

		void acceptClosed(int code, @Nullable String reason);
	}
}
