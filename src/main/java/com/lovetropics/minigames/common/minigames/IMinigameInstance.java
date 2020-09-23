package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.map.MapRegions;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;

import java.util.Set;
import java.util.function.Consumer;

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
    void addControlCommand(String name, Consumer<CommandSource> task);

    void invokeControlCommand(String name, CommandSource source);

    Set<String> getControlCommands();

    /**
     * @return The list of all players that are a part of this minigame instance.
     */
    PlayerSet getPlayers();

    /**
     * @return The list of players within this minigame instance that belong to the given role
     */
    PlayerSet getPlayersForRule(PlayerRole role);

    /**
     * @return The list of active participants that are playing within the minigame instance.
     */
    default PlayerSet getParticipants() {
        return getPlayersForRule(PlayerRole.PARTICIPANT);
    }

    /**
     * @return The list of spectators that are observing the minigame instance.
     */
    default PlayerSet getSpectators() {
        return getPlayersForRule(PlayerRole.SPECTATOR);
    }

    /**
     * Used for executing commands of datapacks within the minigames.
     * @return The command source for this minigame instance.
     */
    CommandSource getCommandSource();

    MapRegions getMapRegions();
    
    default ServerWorld getWorld() {
    	return getCommandSource().getWorld();
    }
    
    default DimensionType getDimension() {
    	return getDefinition().getDimension();
    }
}
