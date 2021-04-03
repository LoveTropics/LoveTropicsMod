package com.lovetropics.minigames.common.core.game;

import net.minecraft.util.Unit;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

public interface IPollingGame extends IGamePhase {
    /**
     * Starts this game if it has at least the minimum amount of
     * participants registered to the game as specified by the game definition.
     *
     * @return The result of the start attempt.
     */
    CompletableFuture<GameResult<IActiveGame>> start();

    @Override
    GameResult<Unit> cancel();

    @Nonnull
    @Override
    default IPollingGame asPolling() {
        return this;
    }

    @Nullable
    @Override
    default IActiveGame asActive() {
        return null;
    }

    @Override
    default GameStatus getStatus() {
        return GameStatus.POLLING;
    }
}
