package com.lovetropics.minigames.common.core.integration;

import com.google.common.base.Strings;
import com.google.gson.*;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public interface TelemetrySender {
	Logger LOGGER = LogManager.getLogger("Telemetry");
	Gson GSON = new GsonBuilder()
			.registerTypeAdapter(GameStatistics.class, GameStatistics.SERIALIZER)
			.registerTypeAdapter(PlayerKey.class, PlayerKey.PROFILE_SERIALIZER)
			.create();

	static TelemetrySender open() {
		ConfigLT.CategoryTelemetry telemetry = ConfigLT.TELEMETRY;
		return new Http(() -> {
			StringBuilder url = new StringBuilder();
			url.append(telemetry.baseUrl.get());
			if (telemetry.port.get() > 0) {
				url.append(':');
				url.append(telemetry.port.get());
			}
			return url.toString();
		}, telemetry.authToken::get);
	}

	static TelemetrySender openPoll() {
		ConfigLT.CategoryTelemetry telemetry = ConfigLT.TELEMETRY;
		return new TelemetrySender.Http(() -> "https://polling.lovetropics.com", telemetry.authToken::get);
	}

	default boolean post(final String endpoint, final JsonElement body) {
		return post(endpoint, GSON.toJson(body));
	}

	boolean post(final String endpoint, final String body);

	JsonElement get(final String endpoint);

	final class Http implements TelemetrySender {
		private static final JsonParser PARSER = new JsonParser();

		private final Supplier<String> url;
		private final Supplier<String> authToken;

		public Http(Supplier<String> url, Supplier<String> authToken) {
			this.url = url;
			this.authToken = authToken;
		}

		@Override
		public boolean post(String endpoint, String body) {
			if (this.isDisabled()) {
				return true;
			}

			try {
				LOGGER.debug("Posting {} to {}/{}", body, url.get(), endpoint);

				HttpURLConnection connection = openAuthorizedConnection("POST", endpoint);
				try (OutputStream output = connection.getOutputStream()) {
					IOUtils.write(body, output, StandardCharsets.UTF_8);
				}

				int code = connection.getResponseCode();
				try {
					String payload = IOUtils.toString(connection.getInputStream(), StandardCharsets.UTF_8);
					LOGGER.debug("Received response from post to {}/{}: {}", url.get(), endpoint, payload);
					return true;
				} catch (IOException e) {
					String response = IOUtils.toString(connection.getErrorStream(), StandardCharsets.UTF_8);
					LOGGER.error("Received unexpected response code ({}) from {}/{}: {}", code, url.get(), endpoint, response);
				}
			} catch (Exception e) {
				LOGGER.error("An exception occurred while trying to POST to {}/{}", url.get(), endpoint, e);
			}

			return false;
		}

		@Override
		public JsonElement get(String endpoint) {
			if (this.isDisabled()) {
				return new JsonObject();
			}

			try {
				HttpURLConnection connection = openAuthorizedConnection("GET", endpoint);
				try (InputStream input = connection.getInputStream()) {
					return PARSER.parse(new BufferedReader(new InputStreamReader(input)));
				}
			} catch (Exception e) {
				LOGGER.error("An exception occurred while trying to GET from {}", endpoint, e);
			}

			return new JsonObject();
		}

		private boolean isDisabled() {
			return Strings.isNullOrEmpty(this.url.get()) || Strings.isNullOrEmpty(this.authToken.get());
		}

		private HttpURLConnection openAuthorizedConnection(String method, String endpoint) throws IOException {
			final URL url = new URL(this.url.get() + "/" + endpoint);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(method);
			connection.setDoOutput(true);
			connection.setRequestProperty("User-Agent", "Tropicraft 1.0 (tropicraft.net)");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "Bearer " + authToken.get());
			return connection;
		}
	}

	final class Log implements TelemetrySender {
		public static final Log INSTANCE = new Log();

		private static final Gson GSON = new GsonBuilder()
				.registerTypeAdapter(GameStatistics.class, GameStatistics.SERIALIZER)
				.registerTypeAdapter(PlayerKey.class, PlayerKey.PROFILE_SERIALIZER)
				.setPrettyPrinting()
				.create();

		private Log() {
		}

		@Override
		public boolean post(final String endpoint, final JsonElement body) {
			return post(endpoint, GSON.toJson(body));
		}

		@Override
		public boolean post(String endpoint, String body) {
			LOGGER.info("POST to {}", endpoint);
			LOGGER.info(body);
			return true;
		}

		@Override
		public JsonElement get(String endpoint) {
			return new JsonObject();
		}
	}

	final class Void implements TelemetrySender {
		@Override
		public boolean post(String endpoint, String body) {
			return true;
		}

		@Override
		public JsonElement get(String endpoint) {
			return new JsonObject();
		}
	}
}
