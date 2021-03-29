package com.lovetropics.minigames.common.game_actions;

import com.lovetropics.minigames.common.MoreCodecs;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;

import java.util.UUID;

/**
 * Care package
 */
public class DonationPackageGameAction extends GameAction
{
    public static final Codec<DonationPackageGameAction> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                MoreCodecs.UUID_STRING.fieldOf("uuid").forGetter(c -> c.uuid),
                Codec.STRING.fieldOf("trigger_time").forGetter(c -> c.triggerTime),
                GamePackage.MAP_CODEC.forGetter(c -> c.gamePackage)
        ).apply(instance, DonationPackageGameAction::new);
    });

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
}
