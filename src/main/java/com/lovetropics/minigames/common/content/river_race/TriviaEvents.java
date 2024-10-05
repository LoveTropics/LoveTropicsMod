package com.lovetropics.minigames.common.content.river_race;

import com.lovetropics.minigames.common.content.river_race.behaviour.TriviaBehaviour;
import com.lovetropics.minigames.common.content.river_race.block.TriviaBlockEntity;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

public class TriviaEvents {

    public static final GameEventType<AnswerQuestion> ANSWER_TRIVIA_BLOCK_QUESTION = GameEventType.create(AnswerQuestion.class,
            listeners -> (player, world, pos, triviaBlockEntity, question, answer) -> {
        for (AnswerQuestion listener : listeners) {
            boolean isCorrect = listener.onAnswerQuestion(player, world, pos, triviaBlockEntity, question, answer);
            if (isCorrect) {
                return isCorrect;
            }
        }
        return false;
    });


    public interface AnswerQuestion {
        boolean onAnswerQuestion(ServerPlayer player, ServerLevel world, BlockPos pos,
                                 TriviaBlockEntity triviaBlockEntity,
                                 @Nullable TriviaBehaviour.TriviaQuestion question, String answer);
    }

}
