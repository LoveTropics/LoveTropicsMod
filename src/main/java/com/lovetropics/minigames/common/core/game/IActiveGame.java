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
	 * Adds the player to this game instance with the given role, or if already in the change, changes their role.
	 * The given player will be removed from their former role, if any.
	 *
	 * @param player the player to add
	 * @param role the role to add the player to
	 * @return whether the player was successfully added or if their role was changed
	 */
	boolean addPlayerTo(ServerPlayerEntity player, PlayerRole role);

	/**
	 * Removes the player from this game instance.
	 *
	 * @param player the player to remove
	 * @return whether the given player was successfully removed
	 */
	boolean removePlayer(ServerPlayerEntity player);

	GameResult<Unit> stop(GameStopReason reason);

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

	@Nullable
	default PlayerRole getRoleFor(ServerPlayerEntity player) {
		for (PlayerRole role : PlayerRole.ROLES) {
			if (getPlayersWithRole(role).contains(player)) {
				return role;
			}
		}
		return null;
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
