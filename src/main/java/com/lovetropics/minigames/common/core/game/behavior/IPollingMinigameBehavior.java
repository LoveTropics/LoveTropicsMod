package com.lovetropics.minigames.common.core.game.behavior;

import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.polling.PollingGameInstance;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;

public interface IPollingMinigameBehavior {
	/**
	 * Called when the minigame begins polling
	 *
	 * @param minigame The minigame that is being constructed
	 */
	default void onStartPolling(final PollingGameInstance minigame) {
	}

	/**
	 * Called when a player registers to join this minigame
	 *
	 * @param minigame The current minigame instance.
	 * @param player The player that has been added.
	 * @param role The role that the player has requested
	 */
	default void onPlayerRegister(final PollingGameInstance minigame, ServerPlayerEntity player, @Nullable PlayerRole role) {
	}
}
