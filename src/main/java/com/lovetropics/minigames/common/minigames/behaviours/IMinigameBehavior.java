package com.lovetropics.minigames.common.minigames.behaviours;

import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public interface IMinigameBehavior
{
	/**
	 * Helper method to define unique logic for the minigame as it is running. Only
	 * called when a minigame using this definition is actively running.
	 * 
	 * @param minigame The current minigame instance.
	 * 
	 * @param world    The world to run this for, currently a worldUpdate call
	 *                 happens per each loaded world
	 */
	default void worldUpdate(final IMinigameInstance minigame, World world) {}

	/**
	 * Helper method to catch when a player dies while inside an active minigame
	 * using this definition. Useful for unique logic defined by this minigame
	 * definition.
	 * 
	 * @param minigame The current minigame instance.
	 * 
	 * @param player   The player which died.
	 */
	default void onPlayerDeath(final IMinigameInstance minigame, ServerPlayerEntity player) {}

	/**
	 * Helper method to create unique logic for when entities in the dimension
	 * update.
	 * 
	 * @param minigame The current minigame instance.
	 * 
	 * @param entity   The entity which is updating.
	 */
	default void onLivingEntityUpdate(final IMinigameInstance minigame, LivingEntity entity) {}

	/**
	 * Helper method to create unique logic for when the player updates.
	 * 
	 * @param minigame The current minigame instance.
	 * 
	 * @param player   The player which is updating.
	 */
	default void onPlayerUpdate(final IMinigameInstance minigame, ServerPlayerEntity player) {}

	/**
	 * Helper method to catch when a player respawns while inside an active minigame
	 * using this definition. Useful for unique logic defined by this minigame
	 * definition.
	 * 
	 * @param minigame The current minigame instance.
	 * 
	 * @param player   The player which died.
	 */
	default void onPlayerRespawn(final IMinigameInstance minigame, ServerPlayerEntity player) {}

	/**
	 * For when a minigame finishes. Useful for cleanup related to this minigame
	 * definition.
	 * 
	 * @param minigame      The current minigame instance.
	 * 
	 * @param commandSource Command source for the minigame instance. Can be used to
	 *                      execute some commands for the minigame from a datapack.
	 */
	default void onFinish(final IMinigameInstance minigame) {}

	/**
	 * For when the minigame has finished and all players are teleported out of the
	 * dimension.
	 * 
	 * @param minigame      The current minigame instance.
	 * @param commandSource Command source for the minigame instance. Can be used to
	 *                      execute some commands for the minigame from a datapack.
	 */
	default void onPostFinish(final IMinigameInstance minigame) {}

	/**
	 * For when a minigame starts. Useful for preparing the minigame.
	 *
	 * @param minigame      The current minigame instance.
	 * @param commandSource Command source for the minigame instance. Can be used to
	 *                      execute some commands for the minigame from a datapack.
	 */
	default void onStart(final IMinigameInstance minigame) {}

	/**
	 * For before a minigame starts. Useful for preparing the minigame.
	 * 
	 * @param definition The definition of the minigame that is about to be started.
	 * @param server     The current minecraft server object.
	 */
	default void onPreStart(final IMinigameDefinition definition, MinecraftServer server) {}

	/**
	 * Event method for players that are hurt in the minigame instance.
	 * 
	 * @param minigame The current minigame instance.
	 * @param event    The living hurt event.
	 */
	default void onPlayerHurt(final IMinigameInstance minigame, LivingHurtEvent event) {}

	/**
	 * Event method for when a player attacks an entity in the minigame instance.
	 * 
	 * @param minigame The current minigame instance.
	 * @param event    The attack entity event.
	 */
	default void onPlayerAttackEntity(final IMinigameInstance minigame, AttackEntityEvent event) {}

	// TODO: document
	default void onAddPlayer(final IMinigameInstance minigame, ServerPlayerEntity player) {
	}

	default void onRemovePlayer(final IMinigameInstance minigame, ServerPlayerEntity player) {
	}

	default void onAddParticipant(final IMinigameInstance minigame, ServerPlayerEntity player) {
	}

	default void onRemoveParticipant(final IMinigameInstance minigame, ServerPlayerEntity player) {
	}

	default void onAddSpectator(final IMinigameInstance minigame, ServerPlayerEntity player) {
	}

	default void onRemoveSpectator(final IMinigameInstance minigame, ServerPlayerEntity player) {
	}
}
