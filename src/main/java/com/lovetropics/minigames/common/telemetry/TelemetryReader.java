package com.lovetropics.minigames.common.telemetry;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;

import java.util.concurrent.CompletableFuture;

public final class TelemetryReader implements WebSocketListener {
	private static final int REQUEST_TIMEOUT = 10000;

	private static final JsonParser PARSER = new JsonParser();

	private final AsyncHttpClient client;
	private final Listener listener;

	private StringBuilder packet;

	TelemetryReader(AsyncHttpClient client, Listener listener) {
		this.client = client;
		this.listener = listener;
	}

	public static CompletableFuture<TelemetryReader> open(String url, Listener listener) {
		AsyncHttpClient client = Dsl.asyncHttpClient();
		TelemetryReader connection = new TelemetryReader(client, listener);

		WebSocketUpgradeHandler upgradeHandler = new WebSocketUpgradeHandler.Builder()
				.addWebSocketListener(connection)
				.build();

		return client.prepareGet(url)
				.setRequestTimeout(REQUEST_TIMEOUT)
				.execute(upgradeHandler)
				.toCompletableFuture()
				.thenApply(ws -> connection);
	}

	@Override
	public void onOpen(WebSocket websocket) {
	}

	@Override
	public void onClose(WebSocket websocket, int code, String reason) {
		listener.onClose(code, reason);
	}

	@Override
	public void onTextFrame(String payload, boolean finalFragment, int rsv) {
		if (packet == null) {
			packet = new StringBuilder(payload);
		} else {
			packet.append(payload);
		}

		if (finalFragment) {
			JsonObject object = PARSER.parse(packet.toString()).getAsJsonObject();
			packet = null;

			listener.onReceivePayload(object);
		}
	}

	@Override
	public void onError(Throwable t) {
		listener.onError(t);
	}

	public boolean isClosed() {
		return client.isClosed();
	}

	interface Listener {
		void onReceivePayload(JsonObject object);

		void onClose(int code, String reason);

		void onError(Throwable t);
	}
}
