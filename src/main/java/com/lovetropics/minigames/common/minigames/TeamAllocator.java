package com.lovetropics.minigames.common.minigames;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.lovetropics.minigames.common.minigames.behaviours.instances.TeamsBehavior;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public final class TeamAllocator {
	private final List<TeamsBehavior.TeamKey> teams;
	private final List<ServerPlayerEntity> players = new ArrayList<>();
	private final Map<ServerPlayerEntity, TeamsBehavior.TeamKey> teamPreferences = new Object2ObjectOpenHashMap<>();

	public TeamAllocator(List<TeamsBehavior.TeamKey> teams) {
		this.teams = teams;
	}

	public void addPlayer(ServerPlayerEntity player, @Nullable TeamsBehavior.TeamKey teamPreference) {
		players.add(player);
		teamPreferences.put(player, teamPreference);
	}

	public void allocate(BiConsumer<ServerPlayerEntity, TeamsBehavior.TeamKey> applyTeam) {
		Multimap<TeamsBehavior.TeamKey, ServerPlayerEntity> teamToPlayers = HashMultimap.create();
		Map<ServerPlayerEntity, TeamsBehavior.TeamKey> playerToTeam = new Object2ObjectOpenHashMap<>();

		Collections.shuffle(players);

		// 1. place everyone in all the teams in an even distribution
		int teamIndex = 0;
		for (ServerPlayerEntity player : players) {
			TeamsBehavior.TeamKey team = teams.get(teamIndex++ % teams.size());
			teamToPlayers.put(team, player);
			playerToTeam.put(player, team);
		}

		Collections.shuffle(players);

		// 2. go through and try to swap players whose preferences mismatch with their assigned team
		for (ServerPlayerEntity player : players) {
			TeamsBehavior.TeamKey preference = teamPreferences.get(player);
			TeamsBehavior.TeamKey current = playerToTeam.get(player);

			if (preference != null && current != preference) {
				// mismatch in preference and assigned team: try swap with another player

				Collection<ServerPlayerEntity> swapCandidates = teamToPlayers.get(preference);
				ServerPlayerEntity swapWith = null;

				for (ServerPlayerEntity swapCandidate : swapCandidates) {
					TeamsBehavior.TeamKey swapCandidatePreference = teamPreferences.get(swapCandidate);
					if (swapCandidatePreference == preference) {
						// we can't swap with this player: they are already in their chosen team
						continue;
					}

					// we want to prioritise swapping with someone who wants to join our team
					if (swapWith == null || swapCandidatePreference == current) {
						swapWith = swapCandidate;
					}
				}

				// we can move if we found someone to swap with or if there is nobody in the other team
				if (swapWith != null || swapCandidates.isEmpty()) {
					teamToPlayers.remove(current, player);
					teamToPlayers.put(preference, player);
					playerToTeam.put(player, preference);
				}

				// move the other player to our team
				if (swapWith != null) {
					teamToPlayers.remove(preference, swapWith);
					teamToPlayers.put(current, swapWith);
					playerToTeam.put(swapWith, current);
				}
			}
		}

		// 3. in theory we have a good allocation of players now: apply these choices to the minigame
		for (Map.Entry<ServerPlayerEntity, TeamsBehavior.TeamKey> entry : playerToTeam.entrySet()) {
			applyTeam.accept(entry.getKey(), entry.getValue());
		}
	}
}
