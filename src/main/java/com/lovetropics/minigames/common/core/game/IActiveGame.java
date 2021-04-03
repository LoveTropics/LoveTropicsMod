package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.integration.GameInstanceTelemetry;
import com.lovetropics.minigames.common.core.map.MapRegions;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Unit;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents an active game instance and all of its state.
 * Managed by a {@link IGameManager} and interfaced with through behaviors and events.
 */
public interface IActiveGame extends IGamePhase {
    /**
     * Finishes the actively running game, indicating that the game exited normally.
     * Generally, this means that games will upload results such as statistics and report a winner to players.
     */
    GameResult<Unit> finish();

    @Override
    GameResult<Unit> cancel();

    /**
     * @return The list of players within this game instance that belong to the given role
     */
    PlayerSet getPlayersWithRole(PlayerRole role);

    boolean setPlayerRole(ServerPlayerEntity player, PlayerRole role);

    /**
     * @return The list of active participants that are playing within the game instance.
     */
    default PlayerSet getParticipants() {
        return getPlayersWithRole(PlayerRole.PARTICIPANT);
    }

    /**
     * @return The list of spectators that are observing the game instance.
     */
    default PlayerSet getSpectators() {
        return getPlayersWithRole(PlayerRole.SPECTATOR);
    }

    @Override
    default int getMemberCount(PlayerRole role) {
    	return getPlayersWithRole(role).size();
    }

    /**
     * Used for executing commands of datapacks within the games.
     * @return The command source for this game instance.
     */
    CommandSource getCommandSource();

    MapRegions getMapRegions();

    /**
     * @return the world that this game takes place within
     */
    ServerWorld getWorld();

    /**
     * @return the dimension that this game takes places within
     */
    RegistryKey<World> getDimension();

    /**
     * @return the tick counter since the game started
     */
    long ticks();

    GameStatistics getStatistics();

    GameInstanceTelemetry getTelemetry();

    GameStateMap getState();

    @Nullable
    @Override
    default IPollingGame asPolling() {
        return null;
    }

    @Nonnull
    @Override
    default IActiveGame asActive() {
        return this;
    }

    @Override
    default GameStatus getStatus() {
        return GameStatus.ACTIVE;
    }
}
