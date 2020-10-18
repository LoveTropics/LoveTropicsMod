package com.lovetropics.minigames.common.techstack.websockets;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lovetropics.minigames.common.config.ConfigLT;
import com.lovetropics.minigames.common.techstack.TechStack;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.ws.WebSocket;
import org.asynchttpclient.ws.WebSocketListener;
import org.asynchttpclient.ws.WebSocketUpgradeHandler;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class WebSocketHelper {

    public static final JsonParser JSON_PARSER = new JsonParser();

    private static final int REQUEST_TIMEOUT = 3000;

    private static AsyncHttpClient ASYNC_HTTP_CLIENT;

    public static JsonObject parse(final String body) {
        // TODO once we know our data model, create an actual Gson object for it
        return JSON_PARSER.parse(body).getAsJsonObject();
    }

    public static boolean cycleConnection() {
        try {
            if (ASYNC_HTTP_CLIENT != null && !ASYNC_HTTP_CLIENT.isClosed()) {
                ASYNC_HTTP_CLIENT.close();
            }

            return open();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void checkAndCycleConnection() {
        if (ASYNC_HTTP_CLIENT == null || ASYNC_HTTP_CLIENT.isClosed()) {
            cycleConnection();
        }
    }

    public static boolean open() {
        if (ASYNC_HTTP_CLIENT == null || ASYNC_HTTP_CLIENT.isClosed()) {
            ASYNC_HTTP_CLIENT = Dsl.asyncHttpClient();
        }
        try {
            WebSocket websocket = ASYNC_HTTP_CLIENT.prepareGet(getUrl()).setRequestTimeout(REQUEST_TIMEOUT)
                .execute(new WebSocketUpgradeHandler.Builder().addWebSocketListener(
                    new WebSocketListener() {
                        @Override
                        public void onOpen(WebSocket websocket) {
                            System.out.println("Web socket opened");
                        }

                        @Override
                        public void onClose(WebSocket websocket, int code, String reason) {
                            System.out.println("Web socket closed");
                        }

                        @Override
                        public void onTextFrame(String payload, boolean finalFragment, int rsv) {
                            TechStack.handleWebSocketPayload(payload);
                        }

                        @Override
                        public void onError(Throwable t) {
                            System.out.println("Error occurred in web socket!");
                            t.printStackTrace();
                        }
                    }).build()).get();
        } catch (InterruptedException | ExecutionException e) {
            System.out.println("Error connecting to web socket. It's probably not running?");
            e.printStackTrace();
        }

        return ASYNC_HTTP_CLIENT != null && !ASYNC_HTTP_CLIENT.isClosed();
    }

    public static void close() {
        if (ASYNC_HTTP_CLIENT != null && !ASYNC_HTTP_CLIENT.isClosed()) {
            try {
                ASYNC_HTTP_CLIENT.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getUrl() {
        final int configPort = ConfigLT.TECH_STACK.webSocketPort.get();
        final String port = configPort == 0 ? "" : ":" + configPort;
        return "ws://" + ConfigLT.TECH_STACK.webSocketUrl.get() + port + "/ws";
    }
}
