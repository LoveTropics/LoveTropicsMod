package com.lovetropics.minigames.common.content.river_race.event;

import com.lovetropics.minigames.common.content.river_race.block.TriviaType;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.impl.MultiGamePhase;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

public class RiverRaceEvents {

    public static final GameEventType<AnswerTriviaQuestion> QUESTION_COMPLETED = GameEventType.create(AnswerTriviaQuestion.class, listeners -> (player, triviaType, triviaPos) -> {
        for (AnswerTriviaQuestion listener : listeners) {
            listener.onAnswer(player, triviaType, triviaPos);
        }
    });

    public static final GameEventType<VictoryPointsChanged> VICTORY_POINTS_CHANGED = GameEventType.create(VictoryPointsChanged.class, listeners -> (team, value, lastValue) -> {
        for (VictoryPointsChanged listener : listeners) {
            listener.onVictoryPointsChanged(team, value, lastValue);
        }
    });

    public static final GameEventType<MicrogameStarted> MICROGAME_STARTED = GameEventType.create(MicrogameStarted.class, listeners -> (game) -> {
        for (MicrogameStarted listener : listeners) {
            listener.onMicrogameStarted(game);
        }
    });

    public static final GameEventType<UnlockZone> UNLOCK_ZONE = GameEventType.create(UnlockZone.class, listeners -> id -> {
        for (UnlockZone listener : listeners) {
            listener.onUnlockZone(id);
        }
    });

    public interface AnswerTriviaQuestion {
        void onAnswer(ServerPlayer player, TriviaType triviaType, BlockPos triviaPos);
    }

    public interface VictoryPointsChanged {
        void onVictoryPointsChanged(GameTeamKey team, int value, int lastValue);
    }

    public interface MicrogameStarted {
        void onMicrogameStarted(MultiGamePhase game);
    }

    public interface UnlockZone {
        void onUnlockZone(String id);
    }
}
