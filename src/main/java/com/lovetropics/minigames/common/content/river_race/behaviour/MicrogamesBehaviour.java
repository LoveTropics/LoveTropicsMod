package com.lovetropics.minigames.common.content.river_race.behaviour;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.GamePhaseType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.config.GameConfig;
import com.lovetropics.minigames.common.core.game.config.GameConfigs;
import com.lovetropics.minigames.common.core.game.impl.GamePhase;
import com.lovetropics.minigames.common.core.game.impl.MultiGamePhase;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.PlayerChatMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record MicrogamesBehaviour(List<ResourceLocation> gameConfigs, int gamesPerRound) implements IGameBehavior {

    public static final MapCodec<MicrogamesBehaviour> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.nonEmptyList(ResourceLocation.CODEC.listOf()).fieldOf("games").forGetter(MicrogamesBehaviour::gameConfigs),
            Codec.INT.optionalFieldOf("games_per_round", 1).forGetter(c -> c.gamesPerRound)
    ).apply(i, MicrogamesBehaviour::new));

    @Override
    public void register(IGamePhase game, EventRegistrar events) throws GameException {
        events.listen(GamePlayerEvents.CHAT, (ServerPlayer player, PlayerChatMessage message) -> {
            queueMicrogames(game);
            //startMicrogame(game);
            startQueuedMicrogame(game);
            return false;
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

    public void startMicrogame(IGamePhase game, String gameName){
        if (game instanceof MultiGamePhase multiGamePhase) {
            GameConfig gameConfig = GameConfigs.REGISTRY.get(ResourceLocation.parse(gameName));
            GamePhase.create(multiGamePhase.game(), gameConfig.getPlayingPhase(), GamePhaseType.PLAYING).thenAccept((result) -> {
                multiGamePhase.setActivePhase(result.getOk(), true);
            });
        }
    }
}
