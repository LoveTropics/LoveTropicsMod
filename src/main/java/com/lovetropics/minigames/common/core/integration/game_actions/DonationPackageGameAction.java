package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.lovetropics.minigames.common.util.MoreCodecs;
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
    public boolean resolve(IGameInstance game, MinecraftServer server) {
        return game.events().invoker(GamePackageEvents.RECEIVE_PACKAGE).onReceivePackage(game, gamePackage);
    }
}
