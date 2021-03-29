package com.lovetropics.minigames.common.minigames.behaviours;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.common.game_actions.GamePackage;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameResult;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;

import java.util.List;

public interface IMinigameBehavior
{
	default ImmutableList<MinigameBehaviorType<? extends IMinigameBehavior>> dependencies() {
		return ImmutableList.of();
	}

	/**
	 * For before a minigame starts. Useful for preparing the minigame.
	 *
	 * @param minigame The minigame that is being constructed
	 */
	default void onConstruct(final IMinigameInstance minigame) {}

	/**
	 * Ensure that this behavior is in a valid state before starting the minigame.
	 * 
	 * @param minigame The current minigame instance, which has not yet been started
	 * @return The result of the check, of type either SUCCESS or FAIL. If FAIL, the
	 *         value will be used as the error message.
	 */
	default MinigameResult<Unit> validateBehavior(final IMinigameInstance minigame) {
		return MinigameResult.ok();
	}

	/**
	 * For when a minigame starts. Useful for preparing the minigame.
	 *
	 * @param minigame      The current minigame instance.
	 */
	default void onStart(final IMinigameInstance minigame) {}

	/**
	 * For when a minigame finishes. Useful for cleanup related to this minigame
	 * definition.
	 * 
	 * @param minigame      The current minigame instance.
	 */
	default void onFinish(final IMinigameInstance minigame) {}

	/**
	 * For when the minigame has finished and all players are teleported out of the
	 * dimension.
	 * 
	 * @param minigame      The current minigame instance.
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
	 *  @param minigame The current minigame instance.
	 *
	 * @param world    The world to run this for, currently a worldUpdate call
	 */
	default void worldUpdate(final IMinigameInstance minigame, ServerWorld world) {}

	/**
	 * Helper method to catch when a player dies while inside an active minigame
	 * using this definition. Useful for unique logic defined by this minigame
	 * definition.
	 *
	 * @param minigame The current minigame instance.
	 * @param player   The player which died.
	 * @param event    The event fired.
	 */
	default void onPlayerDeath(final IMinigameInstance minigame, ServerPlayerEntity player, LivingDeathEvent event) {}

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
	default void onParticipantUpdate(final IMinigameInstance minigame, ServerPlayerEntity player) {}

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
	 * @param minigame The current minigame instance.
	 * @param player   The player that has been added.
	 * @param role     The role that the player has been added to
	 * @param lastRole This player's last role
	 */
	default void onPlayerChangeRole(final IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role, PlayerRole lastRole) {
	}

	/**
	 * Called when a player is removed from this minigame instance. This may be as a spectator or participant
	 *
	 * @param minigame The current minigame instance.
	 * @param player   The player that has been removed.
	 */
	default void onPlayerLeave(final IMinigameInstance minigame, ServerPlayerEntity player) {
	}

	/**
	 * Called when a player interacts with an entity
	 * @param minigame  The current minigame instance.
	 * @param player    The player that interacted with an entity
	 * @param entity    The entity that was interacted with
	 * @param hand      The hand the player used to interact
	 */
	default void onPlayerInteractEntity(final IMinigameInstance minigame, ServerPlayerEntity player, Entity entity, Hand hand) {
	}

	/**
	 * Called when a player left clicks on a block
	 *  @param minigame The current minigame instance.
	 * @param player   The player that left-clicked the block.
	 * @param pos      The block position that was clicked.
	 * @param event     The face that was clicked.
	 */
	default void onPlayerLeftClickBlock(final IMinigameInstance minigame, ServerPlayerEntity player, BlockPos pos, PlayerInteractEvent.LeftClickBlock event) {
	}

	/**
	 * When a care package has been requested by the backend.
	 *
	 * @param minigame The minigame that is being constructed
	 *
	 * @param gamePackage
	 * @return Whether or not the action should considered "handled"
	 * and sent as an acknowledgement to the backend.
	 */
	default boolean onGamePackageReceived(final IMinigameInstance minigame, final GamePackage gamePackage) {
		return false;
	}

	/**
	 * When a block is broken by a player within this minigame.
	 *  @param minigame The current minigame instance
	 * @param player The player that destroyed this block
	 * @param pos The block position that was broken
	 * @param state The block state that was broken
	 * @param event
	 */
	default void onPlayerBreakBlock(IMinigameInstance minigame, ServerPlayerEntity player, BlockPos pos, BlockState state, BlockEvent.BreakEvent event) {
	}

	/**
	 * When a block is placed by an entity within this minigame
	 * @param minigame The current minigame instance
	 * @param entity The entity that destroyed this block
	 * @param pos The block position that was broken
	 * @param state The block state that was broken
	 * @param event
	 */
	default void onEntityPlaceBlock(IMinigameInstance minigame, Entity entity, BlockPos pos, BlockState state, BlockEvent.EntityPlaceEvent event) {
	}

	default void onExplosionDetonate(IMinigameInstance minigame, ExplosionEvent.Detonate event) {

	}

	/**
	 * Called when a chunk within this minigame is loaded
	 * @param minigame the current minigame instance
	 * @param chunk the chunk that was loaded
	 */
	default void onChunkLoad(IMinigameInstance minigame, IChunk chunk) {
	}

	default void assignPlayerRoles(IMinigameInstance minigame, List<ServerPlayerEntity> participants, List<ServerPlayerEntity> spectators) {
	}
}
