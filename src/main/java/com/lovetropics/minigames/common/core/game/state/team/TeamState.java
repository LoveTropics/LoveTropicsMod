package com.lovetropics.minigames.common.core.game.state.team;

import com.google.common.base.Preconditions;
import com.lovetropics.minigames.common.core.game.player.MutablePlayerSet;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.GameStateKey;
import com.lovetropics.minigames.common.core.game.state.IGameState;
import com.lovetropics.minigames.common.core.game.util.TeamAllocator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public final class TeamState implements IGameState, Iterable<TeamKey> {
	public static final GameStateKey<TeamState> KEY = GameStateKey.create("Teams");

	private final List<TeamKey> teams;
	private final Map<TeamKey, MutablePlayerSet> teamPlayers = new Object2ObjectOpenHashMap<>();

	private final Allocations allocations = new Allocations();

	public TeamState(List<TeamKey> teams) {
		this.teams = teams;
	}

	public Allocations getAllocations() {
		return allocations;
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

	@Override
	public Iterator<TeamKey> iterator() {
		return teams.iterator();
	}

	public static final class Allocations {
		private final List<TeamKey> pollingTeams = new ArrayList<>();

		private final Object2IntMap<TeamKey> maxSizes = new Object2IntOpenHashMap<>();

		private final Map<UUID, TeamKey> assignments = new Object2ObjectOpenHashMap<>();
		private final Map<UUID, TeamKey> preferences = new Object2ObjectOpenHashMap<>();

		public void allocate(PlayerSet participants, BiConsumer<ServerPlayerEntity, TeamKey> apply) {
			// apply all direct team assignments first
			for (Map.Entry<UUID, TeamKey> entry : this.assignments.entrySet()) {
				ServerPlayerEntity player = participants.getPlayerBy(entry.getKey());
				if (player != null) {
					apply.accept(player, entry.getValue());
				}
			}

			if (!this.pollingTeams.isEmpty()) {
				TeamAllocator<TeamKey, ServerPlayerEntity> teamAllocator = createAllocator();

				for (ServerPlayerEntity player : participants) {
					UUID uuid = player.getUniqueID();
					if (!assignments.containsKey(uuid)) {
						teamAllocator.addPlayer(player, preferences.get(uuid));
					}
				}

				teamAllocator.allocate(apply);
			}
		}

		private TeamAllocator<TeamKey, ServerPlayerEntity> createAllocator() {
			TeamAllocator<TeamKey, ServerPlayerEntity> teamAllocator = new TeamAllocator<>(pollingTeams);
			for (Object2IntMap.Entry<TeamKey> entry : maxSizes.object2IntEntrySet()) {
				teamAllocator.setSizeForTeam(entry.getKey(), entry.getIntValue());
			}

			return teamAllocator;
		}

		public void addPollingTeam(TeamKey team) {
			this.pollingTeams.add(team);
		}

		public void setPlayerAssignment(UUID player, TeamKey team) {
			this.assignments.put(player, team);
		}

		public void setPlayerPreference(UUID player, TeamKey team) {
			this.preferences.put(player, team);
		}

		public void setMaxTeamSize(TeamKey team, int size) {
			this.maxSizes.put(team, size);
		}

		public List<TeamKey> getPollingTeams() {
			return this.pollingTeams;
		}

		public Collection<UUID> getAssignedPlayers() {
			return this.assignments.keySet();
		}
	}
}
