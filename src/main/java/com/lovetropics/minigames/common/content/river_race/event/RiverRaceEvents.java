package com.lovetropics.minigames.common.content.river_race.event;

import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.state.team.GameTeamKey;
import net.minecraft.server.level.ServerPlayer;

public class RiverRaceEvents {

    public static final GameEventType<AnswerTriviaQuestion> ANSWER_QUESTION = GameEventType.create(AnswerTriviaQuestion.class, listeners -> (player, correct) -> {
        for (AnswerTriviaQuestion listener : listeners) {
            listener.onAnswer(player, correct);
        }
    });

    public static final GameEventType<VictoryPointsChanged> VICTORY_POINTS_CHANGED = GameEventType.create(VictoryPointsChanged.class, listeners -> (team, value, lastValue) -> {
        for (VictoryPointsChanged listener : listeners) {
            listener.onVictoryPointsChanged(team, value, lastValue);
        }
    });

    public interface AnswerTriviaQuestion {
        void onAnswer(ServerPlayer player, final boolean correct);
    }

    public interface VictoryPointsChanged {
        void onVictoryPointsChanged(GameTeamKey team, int value, int lastValue);
    }
}
