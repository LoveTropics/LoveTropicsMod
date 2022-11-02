package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.mojang.serialization.MapCodec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;

/**
 * Care package
 */
public record DonationPackageGameAction(GamePackage gamePackage) implements GameAction {
    public static final MapCodec<DonationPackageGameAction> CODEC = GamePackage.MAP_CODEC
            .xmap(DonationPackageGameAction::new, DonationPackageGameAction::gamePackage);

    @Override
    public boolean resolve(IGamePhase game, MinecraftServer server) {
        InteractionResult result = game.invoker(GamePackageEvents.RECEIVE_PACKAGE).onReceivePackage($ -> {}, gamePackage);
        return result == InteractionResult.SUCCESS;
    }
}
