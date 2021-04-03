package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.util.MoreCodecs;
import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public abstract class GameAction implements Comparable<GameAction> {
    public static final Codec<LocalDateTime> TIME_CODEC = MoreCodecs.localDateTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));

    public final UUID uuid;
    public final LocalDateTime triggerTime;

    protected GameAction(final UUID uuid, final LocalDateTime triggerTime) {
        this.uuid = uuid;
        this.triggerTime = triggerTime;
    }

    /**
     * Resolves the requested action.
     *
     * @param game The minigame that this occurred within
     * @param server The game server the action is resolved on.
     * @return Whether or not to send an acknowledgement back that
     * the action has been resolved.
     */
    public abstract boolean resolve(IActiveGame game, MinecraftServer server);

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj instanceof GameAction) {
            return ((GameAction) obj).uuid.equals(uuid);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public int compareTo(final GameAction other) {
        if (triggerTime != null && other.triggerTime != null) {
            return triggerTime.compareTo(other.triggerTime);
        }
        return 0;
    }
}
