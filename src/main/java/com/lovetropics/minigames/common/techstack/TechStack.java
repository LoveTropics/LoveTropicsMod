package com.lovetropics.minigames.common.techstack;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.game_actions.GameAction;
import com.lovetropics.minigames.common.techstack.websockets.WebSocketHelper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TechStack {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(MinigameStatistics.class, MinigameStatistics.SERIALIZER)
            .create();

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("lt-tech-stack")
                    .setDaemon(true)
                    .build()
    );

    public static void uploadMinigameResults(final String eventName, final MinigameStatistics statistics) {
        final MinigameResult result = new MinigameResult(eventName, statistics);
        uploadMinigameResults(result);
    }

    /**
     *  Expects data in the following format
     *        "name": "Survive the Tide",
     *        "host": "OMGChad",
     *        "participants": [
     *             {
     *                 "name": "cojo",
     *                 "place": 5,
     *                 "score": 4999,
     *                 "score_units": "points"
     *             },
     *             {
     *                 "name": "cojo",
     *                 "place": 4,
     *                 "score": 1,
     *                 "score_units": "points"
     *             },
     *             {
     *                 "name": "tterrag",
     *                 "place": 1,
     *                 "score": 2,
     *                 "score_units": "points"
     *             }
     *         ]
     */
    public static void uploadMinigameResults(MinigameResult result) {
        final String json = GSON.toJson(result);
        post(getUrl(ConfigLT.TECH_STACK.resultsEndpoint.get()), json);
    }

    private static void post(final String url, final String json) {
        EXECUTOR.submit(() -> {
            try {
                HttpURLConnection con = getAuthorizedConnection("POST", url);
                try {
                    try (OutputStream output = con.getOutputStream()) {
                        IOUtils.write(json, output, StandardCharsets.UTF_8);
                    }

                    // Print result
                    int HttpResult = con.getResponseCode();
                    if (HttpResult == HttpURLConnection.HTTP_OK) {
                        try (InputStream input = con.getInputStream()) {
                            System.out.println(IOUtils.toString(input, StandardCharsets.UTF_8));
                        }
                    } else {
                        System.out.println(con.getResponseMessage());
                    }
                } catch (IOException ex) {
                    // TODO - do we need this still?
                    // LogManager.getLogger().error(readInput(con.getErrorStream(), true));
                } finally {
                    con.disconnect();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private static HttpURLConnection getAuthorizedConnection(String method, String address) throws IOException {
        final URL url = new URL(address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(method);
        con.setDoOutput(true);
        con.setRequestProperty("User-Agent", "Tropicraft 1.0 (tropicraft.net)");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Authorization", "Bearer " + ConfigLT.TECH_STACK.authToken.get());
        return con;
    }

    private static String getUrl(final String endpoint) {
        StringBuilder builder = new StringBuilder();
        builder.append(ConfigLT.TECH_STACK.baseUrl.get());
        if (ConfigLT.TECH_STACK.port.get() > 0) {
            builder.append(':');
            builder.append(ConfigLT.TECH_STACK.port.get());
        }
        builder.append('/');
        builder.append(endpoint);
        return builder.toString();
    }

    public static void handleWebSocketPayload(final String payload) {
        final JsonObject obj = WebSocketHelper.parse(payload);
        final String type = obj.get("type").getAsString();
        final String crud = obj.get("crud").getAsString();
        System.out.println("Payload Received");

        if (crud.equals("create")) {
            BackendRequest.getFromId(type).ifPresent(request ->
                    GameActionHandler.queueGameAction(request, request.getGameActionFactory().apply(obj.getAsJsonObject("payload"))));
        }
    }

    public static void acknowledgeActionDelivery(final BackendRequest request, final GameAction action) {
        final JsonObject jsonObj = new JsonObject();

        jsonObj.addProperty("request", request.getId());
        jsonObj.addProperty("uuid", action.uuid.toString());

        post(getUrl(ConfigLT.TECH_STACK.actionResolvedEndpoint.get()), jsonObj.getAsString());
    }
}
