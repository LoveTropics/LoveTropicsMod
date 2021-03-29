package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.map.MapRegions;
import com.lovetropics.minigames.common.core.game.behavior.BehaviorDispatcher;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.statistics.MinigameStatistics;
import com.lovetropics.minigames.common.core.integration.GameInstanceTelemetry;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import java.util.Collection;
import java.util.Optional;

/**
 * An instance used to track which participants and spectators are inside
 * the running minigame. Also holds the definition to process the content
 * within the minigame.
 */
public interface IGameInstance extends ProtoGame, GameControllable, BehaviorDispatcher<IGameBehavior, IGameInstance>
{
    @Override
    Collection<IGameBehavior> getBehaviors();

    <T extends IGameBehavior> Collection<T> getBehaviors(GameBehaviorType<T> type);

    default <T extends IGameBehavior> Optional<T> getOneBehavior(GameBehaviorType<T> type) {
        return getBehaviors(type).stream().findFirst();
    }

    default <T extends IGameBehavior> T getOneBehaviorOrThrow(GameBehaviorType<T> type) {
        return getOneBehavior(type).orElseThrow(RuntimeException::new);
    }

    /**
     * Adds the player to this minigame with the given role, or sets the players role if they are already added.
     * This method will also remove the player from any other role they are contained within.
     *
     * @param player the player to add
     */
    void addPlayer(ServerPlayerEntity player, PlayerRole role);

    /**
     * Removes the player from this minigame.
     * @param player the player to remove
     * @return
     */
    boolean removePlayer(ServerPlayerEntity player);

    /**
     * @return The list of all players that are a part of this minigame instance.
     */
    PlayerSet getPlayers();

    /**
     * @return The list of players within this minigame instance that belong to the given role
     */
    PlayerSet getPlayersWithRole(PlayerRole role);

    /**
     * @return The list of active participants that are playing within the minigame instance.
     */
    default PlayerSet getParticipants() {
        return getPlayersWithRole(PlayerRole.PARTICIPANT);
    }

    /**
     * @return The list of spectators that are observing the minigame instance.
     */
    default PlayerSet getSpectators() {
        return getPlayersWithRole(PlayerRole.SPECTATOR);
    }

    @Override
    default int getMemberCount(PlayerRole role) {
    	return getPlayersWithRole(role).size();
    }

    /**
     * Used for executing commands of datapacks within the minigames.
     * @return The command source for this minigame instance.
     */
    CommandSource getCommandSource();

    MapRegions getMapRegions();

    ServerWorld getWorld();

    /**
     * The targeted dimension you'd like this minigame to teleport players to
     * when they join as players or spectators.
     * @return The dimension type players are teleported to when joining.
     */
    RegistryKey<World> getDimension();

    default void update() {}

    /**
     * @return The ticks since minigame start
     */
    long ticks();

    MinigameStatistics getStatistics();

    GameInstanceTelemetry getTelemetry();

    default void close() {}
}
