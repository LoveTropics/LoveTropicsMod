package com.lovetropics.minigames.common.game_actions;

import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

/**
 * Care package
 */
public class DonationPackageGameAction extends GameAction
{
    private final GamePackage gamePackage;

    public DonationPackageGameAction(UUID uuid, String triggerTime, final GamePackage gamePackage) {
        super(uuid, triggerTime);

        this.gamePackage = gamePackage;
    }

    public GamePackage getGamePackage() {
        return gamePackage;
    }

    @Override
    public boolean resolve(IMinigameInstance minigame, MinecraftServer server) {
        boolean resolved = false;
        for (IMinigameBehavior behavior : minigame.getBehaviors()) {
            resolved |= behavior.onGamePackageReceived(minigame, gamePackage);
        }

        return resolved;
    }

    public static DonationPackageGameAction fromJson(final JsonObject obj) {
        final UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
        final String packageType = obj.get("package_type").getAsString();
        final String triggerTime = obj.get("trigger_time").getAsString();

        final String sendingPlayerName =
                obj.has("sending_player_name") ? obj.get("sending_player_name").getAsString() : "Anonymous";
        final UUID receivingPlayer;

        if (obj.has("receiving_player")) {
            receivingPlayer = UUID.fromString(obj.get("receiving_player").getAsString());
        } else {
            receivingPlayer = null;
        }

        return new DonationPackageGameAction(uuid, triggerTime, new GamePackage(packageType, sendingPlayerName, receivingPlayer));
    }
}
