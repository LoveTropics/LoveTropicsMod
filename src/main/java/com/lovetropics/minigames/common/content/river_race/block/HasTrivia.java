package com.lovetropics.minigames.common.content.river_race.block;

import com.lovetropics.minigames.common.content.river_race.behaviour.TriviaBehaviour;

public interface HasTrivia {

    boolean hasQuestion();

    void setQuestion(TriviaBehaviour.TriviaQuestion question);

    TriviaBehaviour.TriviaQuestion getQuestion();

    TriviaBlock.TriviaType getTriviaType();

    long lockout(int lockoutSeconds);

    void unlock();

    void markAsCorrect();

    TriviaBlockEntity.TriviaBlockState getState();
}
