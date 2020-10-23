package com.lovetropics.minigames.common.game_actions;

import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

/**
 * Care package
 */
public class CarePackageGameAction extends MinigameGameAction
{
    private final String sendingPlayerName;
    private final UUID receivingPlayer;

    public CarePackageGameAction(UUID uuid, String triggerTime, final String sendingPlayerName, final UUID receivingPlayer) {
        super(uuid, triggerTime);

        this.sendingPlayerName = sendingPlayerName;
        this.receivingPlayer = receivingPlayer;
    }

    public String getSendingPlayerName()
    {
        return sendingPlayerName;
    }

    public UUID getReceivingPlayer()
    {
        return receivingPlayer;
    }

    @Override
    public boolean notifyBehavior(IMinigameInstance instance, IMinigameBehavior behavior)
    {
        return behavior.onCarePackageRequested(instance, this);
    }

    public static CarePackageGameAction fromJson(final JsonObject obj) {
        final UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
        final String triggerTime = obj.get("trigger_time").getAsString();

        final String sendingPlayerName = obj.get("sending_player_name").getAsString();
        final UUID receivingPlayer = UUID.fromString(obj.get("receiving_player").getAsString());

        return new CarePackageGameAction(uuid, triggerTime, sendingPlayerName, receivingPlayer);
    }
}
