package com.lovetropics.minigames.common.minigames.behaviours;

import com.lovetropics.minigames.common.minigames.IMinigameDefinition;
import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public interface IMinigameBehavior
{
	/**
	 * Helper method to define unique logic for the minigame as it is
	 * running. Only called when a minigame using this definition is
	 * actively running.
	 *
	 * @param world The world to run this for, currently a worldUpdate call happens per each loaded world
	 * @param instance The instance of the currently running minigame.
	 */
	default void worldUpdate(final IMinigameDefinition definition, World world, IMinigameInstance instance) {}

	/**
	 * Helper method to catch when a player dies while inside an active
	 * minigame using this definition. Useful for unique logic defined
	 * by this minigame definition.
	 *
	 * @param player The player which died.
	 * @param instance The instance of the currently running minigame.
	 */
	default void onPlayerDeath(final IMinigameDefinition definition, ServerPlayerEntity player, IMinigameInstance instance) {}

	/**
	 * Helper method to create unique logic for when entities in the dimension update.
	 * @param entity The entity which is updating.
	 * @param instance The instance of the currently running minigame.
	 */
	default void onLivingEntityUpdate(final IMinigameDefinition definition, LivingEntity entity, IMinigameInstance instance) {}

	/**
	 * Helper method to create unique logic for when the player updates.
	 * @param player The player which is updating.
	 * @param instance The instance of the currently running minigame.
	 */
	default void onPlayerUpdate(final IMinigameDefinition definition, ServerPlayerEntity player, IMinigameInstance instance) {}

	/**
	 * Helper method to catch when a player respawns while inside an active
	 * minigame using this definition. Useful for unique logic defined
	 * by this minigame definition.
	 *
	 * @param player The player which died.
	 * @param instance The instance of the currently running minigame.
	 */
	default void onPlayerRespawn(final IMinigameDefinition definition, ServerPlayerEntity player, IMinigameInstance instance) {}

	/**
	 * For when a minigame finishes. Useful for cleanup related to this
	 * minigame definition.
	 *
	 * @param commandSource Command source for the minigame instance.
	 *                      Can be used to execute some commands for
	 *                      the minigame from a datapack.
	 * @param instance The current minigame instance.
	 */
	default void onFinish(final IMinigameDefinition definition, CommandSource commandSource, IMinigameInstance instance) {}

	/**
	 * For when the minigame has finished and all players are teleported
	 * out of the dimension.
	 * @param commandSource Command source for the minigame instance.
	 *                      Can be used to execute some commands for
	 *                      the minigame from a datapack.
	 */
	default void onPostFinish(final IMinigameDefinition definition, CommandSource commandSource) {}

	/**
	 * For when a minigame starts. Useful for preparing the minigame.
	 *
	 * @param commandSource Command source for the minigame instance.
	 *                      Can be used to execute some commands for
	 *                      the minigame from a datapack.
	 * @param instance The current minigame instance.
	 */
	default void onStart(final IMinigameDefinition definition, CommandSource commandSource, IMinigameInstance instance) {}

	/**
	 * For before a minigame starts. Useful for preparing the minigame.
	 */
	default void onPreStart(final IMinigameDefinition definition, MinecraftServer server) {}

	/**
	 * Event method for players that are hurt in the minigame instance.
	 * @param event The living hurt event.
	 * @param instance The minigame instance.
	 */
	default void onPlayerHurt(final IMinigameDefinition definition, LivingHurtEvent event, IMinigameInstance instance) {}

	/**
	 * Event method for when a player attacks an entity in the minigame instance.
	 * @param event The attack entity event.
	 * @param instance The minigame instance.
	 */
	default void onPlayerAttackEntity(final IMinigameDefinition definition, AttackEntityEvent event, IMinigameInstance instance) {}
}
