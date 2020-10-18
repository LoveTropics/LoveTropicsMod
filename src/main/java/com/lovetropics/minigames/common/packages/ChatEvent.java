package com.lovetropics.minigames.common.packages;

import com.google.gson.JsonObject;

import java.util.UUID;

/**
 * Chat-caused event
 */
public class ChatEvent extends GameAction {

    public ChatEvent(UUID uuid, String triggerTime) {
        super(uuid, triggerTime);
    }

    public static ChatEvent fromJson(final JsonObject obj) {
        final JsonObject payload = obj.getAsJsonObject("payload");
        // TODO actually create the right thing here
        return new ChatEvent(null, null);
    }
}
