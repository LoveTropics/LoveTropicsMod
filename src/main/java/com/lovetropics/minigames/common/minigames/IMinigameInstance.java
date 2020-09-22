package com.lovetropics.minigames.common.minigames;

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
     * Adds the given player into the collection of active participants within this minigame.
     * This method will also remove the given player from the spectators collection
     *
     * @param player the player to add
     * @throws IllegalArgumentException if the player is not already apart of this game
     */
    void makeParticipant(ServerPlayerEntity player) throws IllegalArgumentException;

    /**
     * Adds the given player into the collection of spectators within this minigame.
     * This method will also remove the given player from the active participants collection
     *
     * @param player the player to add
     * @throws IllegalArgumentException if the player is not already apart of this game
     */
    void makeSpectator(ServerPlayerEntity player) throws IllegalArgumentException;

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
    MutablePlayerSet getAllPlayers();

    /**
     * @return The list of active participants that are playing within the minigame instance.
     */
    PlayerSet getParticipants();

    /**
     * @return The list of spectators that are observing the minigame instance.
     */
    PlayerSet getSpectators();

    /**
     * Used for executing commands of datapacks within the minigames.
     * @return The command source for this minigame instance.
     */
    CommandSource getCommandSource();
    
    default ServerWorld getWorld() {
    	return getCommandSource().getWorld();
    }
    
    default DimensionType getDimension() {
    	return getDefinition().getDimension();
    }
}
