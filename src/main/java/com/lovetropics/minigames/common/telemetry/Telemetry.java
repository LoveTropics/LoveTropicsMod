package com.lovetropics.minigames.common.telemetry;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.game_actions.GameAction;
import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class Telemetry {
	public static final Telemetry INSTANCE = new Telemetry();

	private static final long RECONNECT_INTERVAL = 60 * 1000;

	private static final Logger LOGGER = LogManager.getLogger(Telemetry.class);

	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
			new ThreadFactoryBuilder()
					.setNameFormat("lt-telemetry")
					.setDaemon(true)
					.build()
	);

	private final TelemetrySender sender = TelemetrySender.Http.openFromConfig();
	private TelemetryReader reader;
	private boolean readerConnecting;

	private long lastReconnectTime;

	private Telemetry() {
	}

	@SubscribeEvent
	public static void tick(TickEvent.ServerTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			Telemetry.INSTANCE.tick();
		}
	}

	private void tick() {
		if (!isReaderConnected() && !readerConnecting) {
			long time = System.currentTimeMillis();

			if (time - lastReconnectTime >= RECONNECT_INTERVAL && !Strings.isNullOrEmpty(ConfigLT.TELEMETRY.authToken.get())) {
				openReader().handle((reader, throwable) -> {
					if (throwable == null) {
						this.reader = reader;
					} else {
						this.reader = null;
						LOGGER.error("Failed to open reader!", throwable);
					}
					this.readerConnecting = false;
					return null;
				});

				lastReconnectTime = time;
			}
		}
	}

	public MinigameInstanceTelemetry openMinigame(IMinigameDefinition definition, PlayerKey initiator) {
		return MinigameInstanceTelemetry.open(this, definition, initiator);
	}

	public void acknowledgeActionDelivery(final BackendRequest request, final GameAction action) {
		final JsonObject object = new JsonObject();
		object.addProperty("request", request.getId());
		object.addProperty("uuid", action.uuid.toString());

		post(ConfigLT.TELEMETRY.actionResolvedEndpoint.get(), object);
	}

	void post(final String endpoint, final JsonElement body) {
		EXECUTOR.submit(() -> sender.post(endpoint, body));
	}

	void post(final String endpoint, final String body) {
		EXECUTOR.submit(() -> sender.post(endpoint, body));
	}

	private CompletableFuture<TelemetryReader> openReader() {
		int configPort = ConfigLT.TELEMETRY.webSocketPort.get();
		String port = configPort == 0 ? "" : ":" + configPort;
		String url = "ws://" + ConfigLT.TELEMETRY.webSocketUrl.get() + port + "/ws";

		return TelemetryReader.open(url, new TelemetryReader.Listener() {
			@Override
			public void onReceivePayload(JsonObject object) {
				handlePayload(object);
			}

			@Override
			public void onClose(int code, String reason) {
				LOGGER.warn("Telemetry websocket was closed with code {}: {}", code, reason);
				reader = null;
			}

			@Override
			public void onError(Throwable t) {
				LOGGER.error("Telemetry websocket closed with error", t);
				reader = null;
			}
		});
	}

	private void handlePayload(JsonObject object) {
		final String type = object.get("type").getAsString();
		final String crud = object.get("crud").getAsString();

		if (crud.equals("create")) {
			handleCreatePayload(object, type);
		}
	}

	private void handleCreatePayload(JsonObject object, String type) {
		BackendRequest.getFromId(type).ifPresent(request -> {
			GameAction action = request.getGameActionFactory().apply(object.getAsJsonObject("payload"));
			GameActionHandler.queueGameAction(request, action);
		});
	}

	public boolean isReaderConnected() {
		return reader != null && !reader.isClosed();
	}
}
