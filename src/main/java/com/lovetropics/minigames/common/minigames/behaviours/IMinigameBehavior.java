package com.lovetropics.minigames.common.minigames.behaviours;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public interface IMinigameBehavior
{
	default ImmutableList<IMinigameBehaviorType<?>> dependencies() {
		return ImmutableList.of();
	}
	
	/**
	 * For before a minigame starts. Useful for preparing the minigame.
	 *
	 * @param minigame The minigame that is being constructed
	 * @param server   The current minecraft server object.
	 */
	default void onConstruct(final IMinigameInstance minigame) {}

	/**
	 * Ensure that this behavior is in a valid state before starting the minigame.
	 * 
	 * @param minigame The current minigame instance, which has not yet been started
	 * @return The result of the check, of type either SUCCESS or FAIL. If FAIL, the
	 *         value will be used as the error message.
	 */
	default ActionResult<ITextComponent> ensureValidity(final IMinigameInstance minigame) {
		return new ActionResult<>(ActionResultType.SUCCESS, new StringTextComponent(""));
	}

	/**
	 * For when a minigame starts. Useful for preparing the minigame.
	 *
	 * @param minigame      The current minigame instance.
	 * @param commandSource Command source for the minigame instance. Can be used to
	 *                      execute some commands for the minigame from a datapack.
	 */
	default void onStart(final IMinigameInstance minigame) {}

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
	 * For when the minigame is canceled by a server operator.
	 *
	 * @param minigame The current minigame instance.
	 */
	default void onCancel(final IMinigameInstance minigame) {}

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

	/**
	 * Called when a player is added to this minigame instance.
	 *
	 * @param minigame The current minigame instance.
	 * @param player   The player that has been added.
	 * @param role     The role that the player has been added to
	 */
	default void onPlayerJoin(final IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
	}

	/**
	 * Called when a player when the player changes role
	 *
	 * @param minigame The current minigame instance.
	 * @param player   The player that has been added.
	 * @param role     The role that the player has been added to
	 */
	default void onPlayerChangeRole(final IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
	}

	/**
	 * Called when a player is removed from this minigame instance. This may be as a spectator or participant
	 *
	 * @param minigame The current minigame instance.
	 * @param player   The player that has been removed.
	 */
	default void onPlayerLeave(final IMinigameInstance minigame, ServerPlayerEntity player) {
	}
}
