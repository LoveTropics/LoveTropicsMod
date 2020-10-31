package com.lovetropics.minigames.common.game_actions;

import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Care package
 */
public class DonationPackageGameAction extends MinigameGameAction
{
    private final String packageType;
    private final String sendingPlayerName;
    @Nullable
    private final UUID receivingPlayer;

    public DonationPackageGameAction(UUID uuid, String packageType, String triggerTime, final String sendingPlayerName, @Nullable final UUID receivingPlayer) {
        super(uuid, triggerTime);

        this.packageType = packageType;
        this.sendingPlayerName = sendingPlayerName;
        this.receivingPlayer = receivingPlayer;
    }

    public String getPackageType() {
        return packageType;
    }

    public String getSendingPlayerName()
    {
        return sendingPlayerName;
    }

    @Nullable
    public UUID getReceivingPlayer()
    {
        return receivingPlayer;
    }

    @Override
    public boolean notifyBehavior(IMinigameInstance instance, IMinigameBehavior behavior)
    {
        return behavior.onDonationPackageRequested(instance, this);
    }

    public static DonationPackageGameAction fromJson(final JsonObject obj) {
        final UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
        final String packageType = obj.get("package_type").getAsString();
        final String triggerTime = obj.get("trigger_time").getAsString();

        final String sendingPlayerName = obj.get("sending_player_name").getAsString();
        final UUID receivingPlayer;

        if (obj.has("receiving_player")) {
            receivingPlayer = UUID.fromString(obj.get("receiving_player").getAsString());
        } else {
            receivingPlayer = null;
        }

        return new DonationPackageGameAction(uuid, packageType, triggerTime, sendingPlayerName, receivingPlayer);
    }
}
