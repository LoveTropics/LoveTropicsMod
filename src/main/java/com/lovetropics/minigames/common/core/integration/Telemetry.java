package com.lovetropics.minigames.common.core.integration;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lovetropics.lib.backend.BackendConnection;
import com.lovetropics.lib.backend.BackendProxy;
import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = Constants.MODID)
public final class Telemetry {
	public static final Telemetry INSTANCE = new Telemetry();

	private static final Logger LOGGER = LogManager.getLogger(Telemetry.class);

	private static final int RETRY_DELAY_SECONDS = 30;
	private static final int MAX_RETRIES = 5;

	private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(
			new ThreadFactoryBuilder()
					.setNameFormat("lt-telemetry")
					.setDaemon(true)
					.build()
	);

	private final TelemetrySender sender = TelemetrySender.open();
	private final TelemetrySender pollSender = TelemetrySender.openPoll();

	private final BackendProxy proxy;

	private GameInstanceTelemetry liveInstance;

	private Telemetry() {
		Supplier<URI> address = () -> {
			ConfigLT.CategoryTelemetry telemetry = ConfigLT.TELEMETRY;
			if (telemetry.isEnabled()) {
				try {
					int configPort = telemetry.webSocketPort.get();
					String port = configPort == 0 ? ":443" : ":" + configPort;
					return new URI("wss://" + telemetry.webSocketUrl.get() + port + "/ws");
				} catch (URISyntaxException e) {
					LOGGER.warn("Malformed URI", e);
				}
			}

			return null;
		};

		this.proxy = new BackendProxy(address, new BackendConnection.Handler() {
			@Override
			public void acceptOpened() {
			}

			@Override
			public void acceptMessage(JsonObject payload) {
				handlePayload(payload);
			}

			@Override
			public void acceptError(Throwable cause) {
				LOGGER.error("Telemetry websocket closed with error", cause);
			}

			@Override
			public void acceptClosed(int code, @Nullable String reason) {
				LOGGER.error("Telemetry websocket closed with code: {} and reason: {}", code, reason);
			}
		});
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
		proxy.tick();

		GameInstanceTelemetry instance = this.liveInstance;
		if (instance != null) {
			instance.tick(server);
		}
	}

	public GameInstanceTelemetry openGame(IGamePhase game) {
		GameInstanceTelemetry instance = new GameInstanceTelemetry(game, this);
		this.liveInstance = instance;
		return instance;
	}

	void postAndRetry(final String endpoint, final JsonElement body) {
		postAndRetry(endpoint, body, 0);
	}

	private void postAndRetry(final String endpoint, final JsonElement body, final int depth) {
		EXECUTOR.submit(() -> {
			if (!sender.post(endpoint, body)) {
				if (depth <= MAX_RETRIES) {
					EXECUTOR.schedule(() -> {
						postAndRetry(endpoint, body, depth + 1);
					}, RETRY_DELAY_SECONDS, TimeUnit.SECONDS);
				}
			}
		});
	}

	void post(final String endpoint, final JsonElement body) {
		EXECUTOR.submit(() -> sender.post(endpoint, body));
	}

	void post(final String endpoint, final String body) {
		EXECUTOR.submit(() -> sender.post(endpoint, body));
	}

	void postPolling(final String endpoint, final JsonElement body) {
		EXECUTOR.submit(() -> pollSender.post(endpoint, body));
	}

	CompletableFuture<JsonElement> get(final String endpoint) {
		return CompletableFuture.supplyAsync(() -> sender.get(endpoint), EXECUTOR);
	}

	private void handlePayload(JsonObject object) {
		LOGGER.debug("Receive payload over websocket: {}", object);

		try {
			final String type = object.get("type").getAsString();
			final String crud = object.get("crud").getAsString();

			handlePayload(object.getAsJsonObject("payload"), type, crud);
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred while trying to handle payload: {}", object, e);
		}
	}

	private void handlePayload(JsonObject object, String type, String crud) {
		GameInstanceTelemetry liveInstance = this.liveInstance;

		// we can ignore the payload because we will request it again when a minigame starts
		if (liveInstance == null) return;

		liveInstance.handlePayload(object, type, crud);
	}

	public boolean isConnected() {
		return proxy.isConnected();
	}

	void closeInstance(GameInstanceTelemetry instance) {
		if (liveInstance == instance) {
			liveInstance = null;
		}
	}

	public void sendOpen() {
		post(ConfigLT.TELEMETRY.worldLoadEndpoint.get(), "");
	}

	public void sendClose() {
		post(ConfigLT.TELEMETRY.worldUnloadEndpoint.get(), "");
	}
}
