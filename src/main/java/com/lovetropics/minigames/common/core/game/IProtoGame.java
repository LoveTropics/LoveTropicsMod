package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.behavior.BehaviorMap;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.control.GameControlCommands;
import com.lovetropics.minigames.common.core.game.statistics.PlayerKey;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;

import javax.annotation.Nullable;

public interface IProtoGame {
	GameInstanceId getInstanceId();

	IGameDefinition getDefinition();

	MinecraftServer getServer();

	BehaviorMap getBehaviors();

	GameEventListeners getEvents();

	default <T> T invoker(GameEventType<T> eventType) {
		return this.getEvents().invoker(eventType);
	}

	GameControlCommands getControlCommands();

	PlayerKey getInitiator();

	/**
	 * @return The list of all players that are a part of this game, including both participants and spectators.
	 */
	PlayerSet getAllPlayers();

	int getMemberCount(PlayerRole role);

	GameStatus getStatus();

	@Nullable
	IPollingGame asPolling();

	@Nullable
	IActiveGame asActive();

	default boolean isPolling() {
		return getStatus() == GameStatus.POLLING;
	}

	default boolean isActive() {
		return getStatus() == GameStatus.ACTIVE;
	}

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

	/**
	 * Cancels this game instance. This will stop an active game or cancel polling.
	 * If active, this generally means games will not upload results such as statistics or report a winner to players.
	 */
	GameResult<Unit> cancel();
}
