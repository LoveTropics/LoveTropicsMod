package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.network.LoveTropicsNetwork;
import com.lovetropics.minigames.common.core.network.SpectatorPlayerActivityMessage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.TextColor;

public record SpectatorActivityAction(TextColor style) implements IGameBehavior {
    public static final Codec<SpectatorActivityAction> CODEC = RecordCodecBuilder.create(i -> i.group(
            MoreCodecs.COLOR.fieldOf("style").forGetter(SpectatorActivityAction::style)
    ).apply(i, SpectatorActivityAction::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
            game.getSpectators().sendPacket(LoveTropicsNetwork.CHANNEL, new SpectatorPlayerActivityMessage(target.getUUID(), style));
            return true;
        });
    }
}
