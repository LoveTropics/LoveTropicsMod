package com.lovetropics.minigames.common.core.integration.game_actions;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePackageEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;

public record ChatEventGameAction(String trigger) implements GameAction {
    public static final MapCodec<ChatEventGameAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("trigger").forGetter(ChatEventGameAction::trigger)
    ).apply(i, ChatEventGameAction::new));

    // TODO: Make GamePackage system less specific to packages
    @Override
    public boolean resolve(IGamePhase game, MinecraftServer server) {
        GamePackage triggeredPackage = new GamePackage(trigger, null, null);

        InteractionResult result = game.invoker(GamePackageEvents.RECEIVE_PACKAGE).onReceivePackage($ -> {}, triggeredPackage);
        return result == InteractionResult.SUCCESS;
    }
}
