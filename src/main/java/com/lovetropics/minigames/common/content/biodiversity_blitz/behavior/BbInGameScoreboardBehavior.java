package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.ClientBbScoreboardState;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record BbInGameScoreboardBehavior(Vec3 start, Vec3 end, boolean side) implements IGameBehavior {
    public static final MapCodec<BbInGameScoreboardBehavior> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Vec3.CODEC.fieldOf("start").forGetter(BbInGameScoreboardBehavior::start),
            Vec3.CODEC.fieldOf("end").forGetter(BbInGameScoreboardBehavior::end),
            Codec.BOOL.fieldOf("side").forGetter(BbInGameScoreboardBehavior::side)
    ).apply(instance, BbInGameScoreboardBehavior::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(BbEvents.ASSIGN_PLOT, (player, plot) ->
                GameClientState.sendToPlayer(new ClientBbScoreboardState(start, end, side, List.of(
                        Component.literal("Biodiversity Blitz").withStyle(ChatFormatting.BOLD)
                )), player));

        events.listen(BbEvents.UPDATE_SCOREBOARD, (players, components) -> {
            for (ServerPlayer player : players) {
                GameClientState.sendToPlayer(new ClientBbScoreboardState(start, end, side, components), player);
            }
        });
    }
}
