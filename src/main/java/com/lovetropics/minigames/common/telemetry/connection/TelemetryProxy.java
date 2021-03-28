package com.lovetropics.minigames.common.telemetry.connection;

import com.google.gson.JsonObject;
import com.lovetropics.minigames.LoveTropics;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.function.Supplier;

public final class TelemetryProxy implements TelemetryConnection {
	private static final long RECONNECT_INTERVAL_MS = 10 * 1000;

	private final Supplier<URI> address;
	private final Handler receiver;

	private TelemetryConnection connection;
	private boolean connecting;

	private long lastConnectTime;

	public TelemetryProxy(Supplier<URI> address, TelemetryConnection.Handler handler) {
		this.address = address;
		this.receiver = new Handler(handler);
		this.initiateConnection();
	}

	public void tick() {
		if (this.connection == null && !this.connecting) {
			long time = System.currentTimeMillis();
			this.tickDisconnected(time);
		}
	}

	private void tickDisconnected(long time) {
		if (time - this.lastConnectTime > RECONNECT_INTERVAL_MS) {
			this.initiateConnection();
		}
	}

	private void initiateConnection() {
		this.lastConnectTime = System.currentTimeMillis();

		URI address = this.address.get();
		if (address != null) {
			this.connecting = true;

			TelemetryWebSocketConnection.connect(address, this.receiver).handle((connection, throwable) -> {
				if (connection != null) {
					this.onConnectionOpen(connection);
				} else {
					this.onConnectionError(throwable);
				}
				return null;
			});
		}
	}

	private void onConnectionOpen(TelemetryConnection connection) {
		LoveTropics.LOGGER.info("Successfully opened telemetry connection to {}", this.address);
		this.connection = connection;
	}

	private void onConnectionError(Throwable throwable) {
		LoveTropics.LOGGER.error("Failed to open telemetry connection to {}", this.address, throwable);
		this.closeConnection();
	}

	private void closeConnection() {
		this.connection = null;
		this.connecting = false;
		this.lastConnectTime = System.currentTimeMillis();
	}

	@Override
	public boolean send(JsonObject payload) {
		TelemetryConnection connection = this.connection;
		if (connection != null) {
			return connection.send(payload);
		} else {
			return false;
		}
	}

	@Override
	public boolean isConnected() {
		return this.connection != null;
	}

	private class Handler implements TelemetryConnection.Handler {
		private final TelemetryConnection.Handler delegate;

		private Handler(TelemetryConnection.Handler delegate) {
			this.delegate = delegate;
		}

		@Override
		public void acceptOpened() {
			this.delegate.acceptOpened();
		}

		@Override
		public void acceptMessage(JsonObject payload) {
			this.delegate.acceptMessage(payload);
		}

		@Override
		public void acceptError(Throwable cause) {
			this.delegate.acceptError(cause);
			TelemetryProxy.this.closeConnection();
		}

		@Override
		public void acceptClosed(int code, @Nullable String reason) {
			this.delegate.acceptClosed(code, reason);
			TelemetryProxy.this.closeConnection();
		}
	}
}
