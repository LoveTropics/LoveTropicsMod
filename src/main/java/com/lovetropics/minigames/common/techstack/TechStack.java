package com.lovetropics.minigames.common.techstack;

import com.google.gson.Gson;
import com.lovetropics.minigames.common.config.ConfigLT;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TechStack {

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
    public static void uploadMinigameResults(final String eventName, final String host, final List<ParticipantEntry> participants) {
        try {
            final String uri = getUrl(ConfigLT.TECH_STACK.resultsEndpoint.get());
            HttpURLConnection con = getAuthorizedConnection("POST", uri);
            final Gson gson = new Gson();
            final MinigameResult result = new MinigameResult(eventName, host, participants);
            try {
                final String json = gson.toJson(result);
                OutputStreamWriter wr = new OutputStreamWriter(con.getOutputStream());
                wr.write(json);
                wr.flush();

                // Print result
                StringBuilder sb = new StringBuilder();
                int HttpResult = con.getResponseCode();
                if (HttpResult == HttpURLConnection.HTTP_OK) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    br.close();
                    System.out.println("" + sb.toString());
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
