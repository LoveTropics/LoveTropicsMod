package com.lovetropics.minigames.common.telemetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public interface TelemetrySender {
	Logger LOGGER = LogManager.getLogger("Telemetry");
	Gson GSON = new GsonBuilder()
			.registerTypeAdapter(MinigameStatistics.class, MinigameStatistics.SERIALIZER)
			.registerTypeAdapter(PlayerKey.class, PlayerKey.PROFILE_SERIALIZER)
			.create();

	default void post(final String endpoint, final JsonElement body) {
		post(endpoint, GSON.toJson(body));
	}

	void post(final String endpoint, final String body);

	final class Http implements TelemetrySender {
		private final String url;
		private final String authToken;

		public Http(String url, String authToken) {
			this.url = url;
			this.authToken = authToken;
		}

		public static Http openFromConfig() {
			StringBuilder url = new StringBuilder();
			url.append(ConfigLT.TELEMETRY.baseUrl.get());
			if (ConfigLT.TELEMETRY.port.get() > 0) {
				url.append(':');
				url.append(ConfigLT.TELEMETRY.port.get());
			}

			return new Http(url.toString(), ConfigLT.TELEMETRY.authToken.get());
		}

		@Override
		public void post(String endpoint, String body) {
			try {
				HttpURLConnection connection = openAuthorizedConnection("POST", endpoint);
				try {
					LOGGER.debug("Posting {} to {}", body, endpoint);

					try (OutputStream output = connection.getOutputStream()) {
						IOUtils.write(body, output, StandardCharsets.UTF_8);
					}

					int code = connection.getResponseCode();
					if (code == HttpURLConnection.HTTP_OK) {
						try (InputStream input = connection.getInputStream()) {
							String response = IOUtils.toString(input, StandardCharsets.UTF_8);
							LOGGER.debug("Received response from post to {}: {}", endpoint, response);
						}
					} else {
						String response = connection.getResponseMessage();
						LOGGER.error("Received unexpected response code ({}) from {}: {}", code, endpoint, response);
					}
				} finally {
					connection.disconnect();
				}
			} catch (Exception e) {
				LOGGER.error("An exception occurred while trying to POST to {}", endpoint, e);
			}
		}

		private HttpURLConnection openAuthorizedConnection(String method, String endpoint) throws IOException {
			final URL url = new URL(this.url + "/" + endpoint);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			connection.setDoOutput(true);
			connection.setRequestProperty("User-Agent", "Tropicraft 1.0 (tropicraft.net)");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "Bearer " + authToken);
			return connection;
		}
	}

	final class Log implements TelemetrySender {
		public static final Log INSTANCE = new Log();

		private static final Gson GSON = new GsonBuilder()
				.registerTypeAdapter(MinigameStatistics.class, MinigameStatistics.SERIALIZER)
				.registerTypeAdapter(PlayerKey.class, PlayerKey.PROFILE_SERIALIZER)
				.setPrettyPrinting()
				.create();

		private Log() {
		}

		@Override
		public void post(String endpoint, JsonElement body) {
			post(endpoint, GSON.toJson(body));
		}

		@Override
		public void post(String endpoint, String body) {
			LOGGER.info("POST to {}", endpoint);
			LOGGER.info(body);
		}
	}
}
