package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResultType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Care package
 */
public class DonationPackageGameAction extends GameAction
{
    public static final Codec<DonationPackageGameAction> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                MoreCodecs.UUID_STRING.fieldOf("uuid").forGetter(c -> c.uuid),
                TIME_CODEC.fieldOf("trigger_time").forGetter(c -> c.triggerTime),
                GamePackage.MAP_CODEC.forGetter(c -> c.gamePackage)
        ).apply(instance, DonationPackageGameAction::new);
    });

    private final GamePackage gamePackage;

    public DonationPackageGameAction(UUID uuid, LocalDateTime triggerTime, final GamePackage gamePackage) {
        super(uuid, triggerTime);

        this.gamePackage = gamePackage;
    }

    public GamePackage getGamePackage() {
        return gamePackage;
    }

    @Override
    public boolean resolve(IGamePhase game, MinecraftServer server) {
        ActionResultType result = game.invoker(GamePackageEvents.RECEIVE_PACKAGE).onReceivePackage($ -> {}, gamePackage);
        return result == ActionResultType.SUCCESS;
    }
}
