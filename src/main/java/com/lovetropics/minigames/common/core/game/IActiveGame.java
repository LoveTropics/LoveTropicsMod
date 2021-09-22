package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.integration.GameInstanceTelemetry;
import com.lovetropics.minigames.common.core.map.MapRegions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Unit;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;

/**
 * Represents an active game instance and all of its state.
 * Managed by a {@link IGameManager} and interfaced with through behaviors and events.
 */
// TODO: clean up
public interface IActiveGame extends IGamePhase {
	/**
	 * Requests that this player join this game instance with the given role preference.
	 *
	 * @param player the player to add
	 * @param requestedRole the preferred role to add this player as, or null if none
	 * @return whether the given player was successfully added
	 */
	boolean requestPlayerJoin(ServerPlayerEntity player, @Nullable PlayerRole requestedRole);

	/**
	 * Removes the player from this game instance.
	 *
	 * @param player the player to remove
	 * @return whether the given player was successfully removed
	 */
	boolean removePlayer(ServerPlayerEntity player);

	GameResult<Unit> stop(GameStopReason reason);

	boolean setPlayerRole(ServerPlayerEntity player, PlayerRole role);

	/**
	 * @return The list of players within this game instance that belong to the given role
	 */
	PlayerSet getPlayersWithRole(PlayerRole role);

	/**
	 * @return The list of active participants that are playing within the game instance.
	 */
	default PlayerSet getParticipants() {
		return getPlayersWithRole(PlayerRole.PARTICIPANT);
	}

	/**
	 * @return The list of spectators that are observing the game instance.
	 */
	default PlayerSet getSpectators() {
		return getPlayersWithRole(PlayerRole.SPECTATOR);
	}

	/**
	 * @return the world that this game takes place within
	 */
	ServerWorld getWorld();

	/**
	 * @return the dimension that this game takes places within
	 */
	default RegistryKey<World> getDimension() {
		return getWorld().getDimensionKey();
	}

	MapRegions getMapRegions();

	/**
	 * @return the tick counter since the game started
	 */
	long ticks();

	GameStatistics getStatistics();

	GameInstanceTelemetry getTelemetry();
}
