package com.lovetropics.minigames.common.game_actions;

import com.google.gson.JsonObject;
import com.lovetropics.minigames.common.Util;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;

import java.util.UUID;

/**
 * Sabotage package
 */
public class SabotagePackageGameAction extends MinigameGameAction
{
    private final String sendingPlayerName;
    private final UUID receivingPlayer;

    public SabotagePackageGameAction(UUID uuid, String triggerTime, final String sendingPlayerName, final UUID receivingPlayer) {
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
        return behavior.onSabotagePackageRequested(instance, this);
    }

    public static SabotagePackageGameAction fromJson(final JsonObject obj) {
        final UUID uuid = UUID.fromString(obj.get("uuid").getAsString());
        final String triggerTime = obj.get("trigger_time").getAsString();

        final String sendingPlayerName = obj.get("sending_player_name").getAsString();
        final UUID receivingPlayer = UUID.fromString(obj.get("receivig_player").getAsString());

        return new SabotagePackageGameAction(uuid, triggerTime, sendingPlayerName, receivingPlayer);
    }
}
