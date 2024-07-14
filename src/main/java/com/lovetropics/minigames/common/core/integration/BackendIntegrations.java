package com.lovetropics.minigames.common.core.integration;

import com.google.common.base.Suppliers;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lovetropics.lib.backend.BackendConnection;
import com.lovetropics.lib.backend.BackendProxy;
import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@EventBusSubscriber(modid = LoveTropics.ID)
public final class BackendIntegrations {
	private static final Supplier<BackendIntegrations> INSTANCE = Suppliers.memoize(BackendIntegrations::new);

	private static final Logger LOGGER = LogManager.getLogger(BackendIntegrations.class);

	private static final int RETRY_DELAY_SECONDS = 30;
	private static final int MAX_RETRIES = 5;

	private static final ScheduledExecutorService EXECUTOR = Executors.newSingleThreadScheduledExecutor(
			new ThreadFactoryBuilder()
					.setNameFormat("lt-integrations")
					.setDaemon(true)
					.build()
	);

	private final IntegrationSender sender = IntegrationSender.open();
	private final IntegrationSender pollSender = IntegrationSender.openPoll();

	private final BackendProxy proxy;

	@Nullable
	private GameInstanceIntegrations liveInstance;

	private BackendIntegrations() {
		Supplier<URI> address = () -> {
			ConfigLT.CategoryIntegrations integrations = ConfigLT.INTEGRATIONS;
			if (integrations.isEnabled()) {
				try {
					return new URI(integrations.webSocketUrl.get());
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
				LOGGER.error("Integrations websocket closed with error", cause);
			}

			@Override
			public void acceptClosed(int code, @Nullable String reason) {
				LOGGER.error("Integrations websocket closed with code: {} and reason: {}", code, reason);
			}
		});
	}

	public static BackendIntegrations get() {
		return INSTANCE.get();
	}

	@SubscribeEvent
	public static void tick(ServerTickEvent.Post event) {
		final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            get().tick(server);
        }
    }

	private void tick(MinecraftServer server) {
		proxy.tick();

		GameInstanceIntegrations instance = this.liveInstance;
		if (instance != null) {
			instance.tick(server);
		}
	}

	public GameInstanceIntegrations openGame(IGamePhase game) {
		GameInstanceIntegrations instance = new GameInstanceIntegrations(game, this);
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

	private void handlePayload(JsonObject object) {
		LOGGER.debug("Receive payload over websocket: {}", object);

		try {
			final String type = object.get("type").getAsString();
			final Crud crud = Crud.parse(object.get("crud"));
			if (crud == null) {
				LOGGER.error("Encountered unrecognized crud: '{}'", object.get("crud"));
				return;
			}

			handlePayload(object.getAsJsonObject("payload"), type, crud);
		} catch (Exception e) {
			LOGGER.error("An unexpected error occurred while trying to handle payload: {}", object, e);
		}
	}

	private void handlePayload(JsonObject object, String type, Crud crud) {
		GameInstanceIntegrations liveInstance = this.liveInstance;

		// we can ignore the payload because we will request it again when a minigame starts
		if (liveInstance == null) return;

		liveInstance.handlePayload(object, type, crud);
	}

	public boolean isConnected() {
		return proxy.isConnected();
	}

	void closeInstance(GameInstanceIntegrations instance) {
		if (liveInstance == instance) {
			liveInstance = null;
		}
	}

	public void sendOpen() {
		post(ConfigLT.INTEGRATIONS.worldLoadEndpoint.get(), "");
	}

	public void sendClose() {
		post(ConfigLT.INTEGRATIONS.worldUnloadEndpoint.get(), "");
	}
}
