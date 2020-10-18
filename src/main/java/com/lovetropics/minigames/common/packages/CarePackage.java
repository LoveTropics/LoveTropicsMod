package com.lovetropics.minigames.common.packages;

import com.google.gson.JsonObject;

import java.util.UUID;

/**
 * Care package
 */
public class CarePackage extends GameAction {

    public CarePackage(UUID uuid, String triggerTime) {
        super(uuid, triggerTime);
    }

    public static CarePackage fromJson(final JsonObject obj) {
        final JsonObject payload = obj.getAsJsonObject("payload");
        // TODO actually create the right thing here
        return new CarePackage(null, null);
    }
}
