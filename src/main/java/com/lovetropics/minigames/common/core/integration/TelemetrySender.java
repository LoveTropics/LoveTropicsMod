package com.lovetropics.minigames.common.core.integration;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

public interface TelemetrySender {
	Logger LOGGER = LogUtils.getLogger();

	Gson GSON = new GsonBuilder()
			.registerTypeAdapter(GameStatistics.class, GameStatistics.SERIALIZER)
			.registerTypeAdapter(PlayerKey.class, PlayerKey.PROFILE_SERIALIZER)
			.create();

	static TelemetrySender open() {
		ConfigLT.CategoryTelemetry telemetry = ConfigLT.TELEMETRY;
		return new Http(telemetry.baseUrl::get, telemetry.authToken::get);
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
		private static final HttpClient CLIENT = HttpClient.newBuilder().executor(Util.ioPool()).build();

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

				HttpResponse<String> response = CLIENT.send(
						request(endpoint)
								.POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
								.build(),
						HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
				);

				if (response.statusCode() == 200) {
					LOGGER.debug("Received response from post to {}/{}: {}", url.get(), endpoint, response.body());
					return true;
				} else {
					LOGGER.error("Received unexpected response code ({}) from {}/{}: {}", response.statusCode(), url.get(), endpoint, response.body());
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
				HttpResponse<InputStream> response = CLIENT.send(
						request(endpoint).GET().build(),
						HttpResponse.BodyHandlers.ofInputStream()
				);
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8))) {
					return JsonParser.parseReader(reader);
				}
			} catch (Exception e) {
				LOGGER.error("An exception occurred while trying to GET from {}", endpoint, e);
			}

			return new JsonObject();
		}

		private HttpRequest.Builder request(String endpoint) {
			return HttpRequest.newBuilder(URI.create(url.get() + "/" + endpoint))
					.header(HttpHeaders.USER_AGENT, "LTMinigames 1.0 (lovetropics.org)")
					.header(HttpHeaders.CONTENT_TYPE, "application/json")
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + authToken.get());
		}

		private boolean isDisabled() {
			return Strings.isNullOrEmpty(this.url.get()) || Strings.isNullOrEmpty(this.authToken.get());
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
