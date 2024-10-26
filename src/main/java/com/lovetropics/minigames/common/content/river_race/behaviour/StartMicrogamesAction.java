package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.impl.MultiGamePhase;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record StartMicrogamesAction(List<ResourceLocation> gameConfigs, int gamesPerRound) implements IGameBehavior {

    public static final MapCodec<StartMicrogamesAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.nonEmptyList(ResourceLocation.CODEC.listOf()).fieldOf("games").forGetter(StartMicrogamesAction::gameConfigs),
            Codec.INT.optionalFieldOf("games_per_round", 1).forGetter(c -> c.gamesPerRound)
    ).apply(i, StartMicrogamesAction::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GameActionEvents.APPLY, context -> {
            queueMicrogames(game);
            startQueuedMicrogame(game);
            return true;
        });
    }

    public void queueMicrogames(IGamePhase game) {
        if (game instanceof MultiGamePhase multiGamePhase) {
            multiGamePhase.clearQueuedGames();

            final List<ResourceLocation> configs = new ArrayList<>(gameConfigs);
            Collections.shuffle(configs);
            multiGamePhase.queueGames(configs.subList(0, gamesPerRound));
        }
    }

    public void startQueuedMicrogame(final IGamePhase game) {
        if (game instanceof MultiGamePhase multiGamePhase) {
            multiGamePhase.startNextQueuedMicrogame(true);
        }
    }
}
