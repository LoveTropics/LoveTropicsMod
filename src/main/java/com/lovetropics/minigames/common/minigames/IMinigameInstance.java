package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.map.MapRegions;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehaviorType;
import com.lovetropics.minigames.common.minigames.statistics.MinigameStatistics;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * An instance used to track which participants and spectators are inside
 * the running minigame. Also holds the definition to process the content
 * within the minigame.
 */
public interface IMinigameInstance
{
    /**
     * The definition used to define what content the minigame contains.
     * @return The minigame definition.
     */
    IMinigameDefinition getDefinition();

    Collection<IMinigameBehavior> getAllBehaviours();

    <T extends IMinigameBehavior> Optional<T> getBehavior(IMinigameBehaviorType<T> type);

    default <T extends IMinigameBehavior> T getBehaviorOrThrow(IMinigameBehaviorType<T> type) {
        return getBehavior(type).orElseThrow(RuntimeException::new);
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
     */
    void removePlayer(ServerPlayerEntity player);

    /**
     * Adds a command with a custom task that can be used through the /minigame command while this game is active
     * @param name the command name to use
     * @param task the task to run when the command is invoked
     */
    void addControlCommand(String name, ControlCommandHandler task);

    void invokeControlCommand(String name, CommandSource source) throws CommandSyntaxException;

    Set<String> getControlCommands();

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

    /**
     * Used for executing commands of datapacks within the minigames.
     * @return The command source for this minigame instance.
     */
    CommandSource getCommandSource();

    MapRegions getMapRegions();

    MinecraftServer getServer();

    ServerWorld getWorld();

    /**
     * The targeted dimension you'd like this minigame to teleport players to
     * when they join as players or spectators.
     * @return The dimension type players are teleported to when joining.
     */
    DimensionType getDimension();

    default void update() {}

    /**
     * @return The ticks since minigame start
     */
    long ticks();

    MinigameStatistics getStatistics();
}
