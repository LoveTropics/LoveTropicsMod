package com.lovetropics.minigames.common.minigames;

import com.lovetropics.minigames.common.minigames.statistics.PlayerKey;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Specification for a minigame manager. Used to register minigame definitions
 * as well as hold the currently running minigame instance if applicable.
 *
 * Implementations get to define the logic for polling, starting, stopping and
 * registering for polling minigames. Each of these actions return an ActionResult,
 * which are fed into Minecraft Commands to send these messages back to players
 * which execute the commands.
 */
public interface IMinigameManager extends MinigameControllable
{
    /**
     * Get all currently registered minigames.
     * @return A collection of all known minigames.
     */
    Collection<IMinigameDefinition> getAllMinigames();

    /**
     * Holds metadata for which players
     * are participants and which are spectators.
     * @return The actively running minigame instance.
     */
    IMinigameInstance getActiveMinigame();

    /**
     * Finishes the actively running minigame, teleporting players back to
     * their original state before joining the minigame.
     */
    MinigameResult<ITextComponent> finish();

    /**
     * Cancels the actively running minigame. Inherents all logic of {@link IMinigameManager#finish()}
     */
    MinigameResult<ITextComponent> cancel();

    /**
     * Starts polling the minigame.
     * @param minigameId The unique ID of the minigame being polled.
     * @param initiator the player starting this minigame
     * @return The result of the polling attempt.
     */
    MinigameResult<ITextComponent> startPolling(ResourceLocation minigameId, PlayerKey initiator);

    /**
     * Stops polling an actively polling minigame.
     * @return The result of stopping the polling of an actively polling minigame.
     */
    MinigameResult<ITextComponent> stopPolling();

    /**
     * Starts an actively polling minigame if it has at least the minimum amount of
     * participants registered to the minigame, specified by the minigame definition.
     * @return The result of the start attempt.
     */
    CompletableFuture<MinigameResult<ITextComponent>> start();

    /**
     * Registers a player for the currently polling minigame. Puts them in a queue
     * to be selected as either a participant or a spectator when the minigame starts.
     * @param player The player being registered for the currently polling minigame.
     * @param requestedRole The role that this player has requested to join as, or null if they have no preference
     * @return The result of the register attempt.
     */
    MinigameResult<ITextComponent> joinPlayerAs(ServerPlayerEntity player, @Nullable PlayerRole requestedRole);

    /**
     * Unregisters a player for a currently polling minigame if they've registered
     * previously. Removes them from the queue for the minigame if they don't want
     * to be a part of it when it starts.
     * @param player The player being unregistered for the currently polling minigame/
     * @return The result of the unregister attempt.
     */
    MinigameResult<ITextComponent> removePlayer(ServerPlayerEntity player);
}
