package com.lovetropics.minigames.common.core.game;

import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.lobby.IGameLobby;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.map.MapRegions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Unit;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.UUID;

public interface IGamePhase extends IGame {
	IGame getGame();

	@Override
	default IGameLobby getLobby() {
		return getGame().getLobby();
	}

	@Override
	default UUID getUuid() {
		return getGame().getUuid();
	}

	@Override
	default MinecraftServer getServer() {
		return getGame().getServer();
	}

	@Override
	default PlayerKey getInitiator() {
		return getGame().getInitiator();
	}

	@Override
	default PlayerSet getAllPlayers() {
		return getGame().getAllPlayers();
	}

	@Override
	default IGameDefinition getDefinition() {
		return getGame().getDefinition();
	}

	@Override
	default GameStateMap getInstanceState() {
		return getGame().getInstanceState();
	}

	GameStateMap getState();

	GamePhaseType getPhaseType();

	IGamePhaseDefinition getPhaseDefinition();

	<T> T invoker(GameEventType<T> type);

	GameResult<Unit> requestStop(GameStopReason reason);

	/**
	 * Adds the player to this game instance with the given role, or if already in the change, changes their role.
	 * The given player will be removed from their former role, if any.
	 *
	 * @param player the player to add
	 * @param role the role to add the player to
	 * @return whether the player was successfully added or if their role was changed
	 */
	boolean setPlayerRole(ServerPlayer player, @Nullable PlayerRole role);

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
	default PlayerRole getRoleFor(ServerPlayer player) {
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
	ServerLevel getWorld();

	/**
	 * @return the dimension that this game takes places within
	 */
	default ResourceKey<Level> getDimension() {
		return getWorld().dimension();
	}

	MapRegions getMapRegions();

	/**
	 * @return the tick counter since the game started
	 */
	long ticks();
}
