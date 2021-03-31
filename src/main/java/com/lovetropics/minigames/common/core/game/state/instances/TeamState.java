package com.lovetropics.minigames.common.core.game.state.instances;

import com.google.common.base.Preconditions;
import com.lovetropics.minigames.common.core.game.MutablePlayerSet;
import com.lovetropics.minigames.common.core.game.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.GameStateType;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class TeamState implements IGameState {
	public static final GameStateType<TeamState> TYPE = GameStateType.create("Teams");

	private final List<TeamKey> teams;
	private final Map<TeamKey, MutablePlayerSet> teamPlayers = new Object2ObjectOpenHashMap<>();

	public TeamState(List<TeamKey> teams) {
		this.teams = teams;
	}

	public void addPlayerTo(ServerPlayerEntity player, TeamKey team) {
		MutablePlayerSet players = getPlayersForTeamMutable(player.server, team);
		players.add(player);
	}

	public void removePlayer(ServerPlayerEntity player) {
		for (MutablePlayerSet players : teamPlayers.values()) {
			players.remove(player);
		}
	}

	public PlayerSet getPlayersForTeam(TeamKey team) {
		PlayerSet players = teamPlayers.get(team);
		return players != null ? players : PlayerSet.EMPTY;
	}

	private MutablePlayerSet getPlayersForTeamMutable(MinecraftServer server, TeamKey team) {
		MutablePlayerSet players = teamPlayers.get(team);
		if (players == null) {
			Preconditions.checkState(teams.contains(team), "invalid team " + team);
			players = new MutablePlayerSet(server);
			teamPlayers.put(team, players);
		}
		return players;
	}

	@Nullable
	public TeamKey getTeamForPlayer(PlayerEntity player) {
		for (TeamKey team : teams) {
			if (teamPlayers.get(team).contains(player)) {
				return team;
			}
		}
		return null;
	}

	public List<TeamKey> getTeams() {
		return teams;
	}

	@Nullable
	public TeamKey getTeamByKey(String key) {
		for (TeamKey team : teams) {
			if (team.key.equals(key)) {
				return team;
			}
		}
		return null;
	}

	public boolean areSameTeam(Entity source, Entity target) {
		if (!(source instanceof PlayerEntity) || !(target instanceof PlayerEntity)) {
			return false;
		}
		TeamKey sourceTeam = getTeamForPlayer((PlayerEntity) source);
		TeamKey targetTeam = getTeamForPlayer((PlayerEntity) target);
		return Objects.equals(sourceTeam, targetTeam);
	}
}
