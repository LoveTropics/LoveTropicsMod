package com.lovetropics.minigames.common.packages;

import com.google.gson.JsonObject;

import java.util.UUID;

/**
 * Sabotage package
 */
public class SabotagePackage extends GameAction {

    public SabotagePackage(UUID uuid, String triggerTime) {
        super(uuid, triggerTime);
    }

    public static SabotagePackage fromJson(final JsonObject obj) {
        final JsonObject payload = obj.getAsJsonObject("payload");
        // TODO actually create the right thing here
        return new SabotagePackage(null, null);
    }
}
