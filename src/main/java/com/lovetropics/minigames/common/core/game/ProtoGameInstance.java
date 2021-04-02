package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.control.GameControlCommands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;

public interface ProtoGameInstance {
	/**
	 * @return a unique ID representing this game instance
	 */
	String getInstanceId();

	/**
	 * Requests that this player join this game instance with the given role preference.
	 *
	 * @param player the player to add
	 * @param requestedRole the preferred role to add this player as, or null if none
	 *
	 * @return whether the given player was successfully added
	 */
	boolean requestPlayerJoin(ServerPlayerEntity player, @Nullable PlayerRole requestedRole);

	/**
	 * Removes the player from this game instance.
	 * @param player the player to remove
	 *
	 * @return whether the given player was successfully removed
	 */
	boolean removePlayer(ServerPlayerEntity player);

	int getMemberCount(PlayerRole role);

	GameStatus getStatus();

	/**
	 * The definition used to define what content the minigame contains.
	 * @return The minigame definition.
	 */
	IGameDefinition getDefinition();

	MinecraftServer getServer();

	BehaviorMap getBehaviors();

	GameEventListeners getEvents();

	default <T> T invoker(GameEventType<T> eventType) {
		return this.getEvents().invoker(eventType);
	}

	GameControlCommands getControlCommands();
}
