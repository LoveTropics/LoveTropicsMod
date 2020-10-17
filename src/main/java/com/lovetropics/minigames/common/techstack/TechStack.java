package com.lovetropics.minigames.common.techstack;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.lovetropics.minigames.common.config.ConfigLT;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TechStack {
    private static final Gson GSON = new Gson();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(
            new ThreadFactoryBuilder()
                    .setNameFormat("lt-tech-stack")
                    .setDaemon(true)
                    .build()
    );

    public static void uploadMinigameResults(final String eventName, final String host, final List<ParticipantEntry> participants) {
        final MinigameResult result = new MinigameResult(eventName, host, participants);
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
        EXECUTOR.submit(() -> {
            try {
                final String uri = getUrl(ConfigLT.TECH_STACK.resultsEndpoint.get());
                HttpURLConnection con = getAuthorizedConnection("POST", uri);
                try {
                    final String json = GSON.toJson(result);
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
}
