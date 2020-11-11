package com.lovetropics.minigames.common.telemetry;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
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

	private MinigameInstanceTelemetry instance;

	private Telemetry() {
	}

	@SubscribeEvent
	public static void tick(TickEvent.ServerTickEvent event) {
		final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null) {
			return;
		}

		if (event.phase == TickEvent.Phase.END) {
			Telemetry.INSTANCE.tick(server);
		}
	}

	private void tick(MinecraftServer server) {
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

		if (instance != null) {
			instance.tick(server);
		}
	}

	public MinigameInstanceTelemetry openMinigame(IMinigameDefinition definition, PlayerKey initiator) {
		return MinigameInstanceTelemetry.open(this, definition, initiator);
	}

	void post(final String endpoint, final JsonElement body) {
		EXECUTOR.submit(() -> sender.post(endpoint, body));
	}

	void post(final String endpoint, final String body) {
		EXECUTOR.submit(() -> sender.post(endpoint, body));
	}

	CompletableFuture<JsonElement> get(final String endpoint) {
		return CompletableFuture.supplyAsync(() -> sender.get(endpoint), EXECUTOR);
	}

	private CompletableFuture<TelemetryReader> openReader() {
		int configPort = ConfigLT.TELEMETRY.webSocketPort.get();
		String port = configPort == 0 ? "" : ":" + configPort;
		String url = "wss://" + ConfigLT.TELEMETRY.webSocketUrl.get() + port + "/ws";

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
		LOGGER.debug("Receive payload over websocket: {}", object);

		try {
			final String type = object.get("type").getAsString();
			final String crud = object.get("crud").getAsString();

			if (crud.equals("create")) {
				handleCreatePayload(object.getAsJsonObject("payload"), type);
			}
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred while trying to handle payload: {}", object, e);
		}
	}

	private void handleCreatePayload(JsonObject object, String type) {
		MinigameInstanceTelemetry instance = this.instance;

		// we can ignore the payload because we will request it again when a minigame starts
		if (instance == null) return;

		instance.handleCreatePayload(object, type);
	}

	public boolean isReaderConnected() {
		return reader != null && !reader.isClosed();
	}

	void openInstance(MinigameInstanceTelemetry instance) {
		this.instance = instance;
	}

	void closeInstance(MinigameInstanceTelemetry instance) {
		if (this.instance == instance) {
			this.instance = null;
		}
	}

	public void sendOpen() {
		post(ConfigLT.TELEMETRY.worldLoadEndpoint.get(), "");
	}

	public void sendClose() {
		post(ConfigLT.TELEMETRY.worldUnloadEndpoint.get(), "");
	}
}
