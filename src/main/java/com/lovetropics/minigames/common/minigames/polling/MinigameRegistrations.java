package com.lovetropics.minigames.common.minigames.polling;

import com.lovetropics.minigames.common.minigames.PlayerRole;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;

import javax.annotation.Nullable;
import java.util.*;

public final class MinigameRegistrations {
	private final Set<UUID> any = new ObjectOpenHashSet<>();
	private final Set<UUID> participants = new ObjectOpenHashSet<>();
	private final Set<UUID> spectators = new ObjectOpenHashSet<>();

	public void clear() {
		this.any.clear();
		this.participants.clear();
		this.spectators.clear();
	}

	public void collectInto(
			MinecraftServer server,
			Collection<ServerPlayerEntity> participants,
			Collection<ServerPlayerEntity> spectators,
			int maximumParticipants
	) {
		PlayerList players = server.getPlayerList();

		// we have no limit on the number of spectators: we can add them all into the game directly
		for (UUID id : this.spectators) {
			ServerPlayerEntity player = players.getPlayerByUUID(id);
			if (player != null) {
				spectators.add(player);
			}
		}

		tryCollectPlayers(players, this.participants, participants, spectators, maximumParticipants);
		tryCollectPlayers(players, this.any, participants, spectators, maximumParticipants);

		this.clear();
	}

	private void tryCollectPlayers(
			PlayerList players, Set<UUID> from,
			Collection<ServerPlayerEntity> participants,
			Collection<ServerPlayerEntity> spectators,
			int maximumParticipants
	) {
		List<ServerPlayerEntity> selectedPlayers = new ArrayList<>(from.size());
		for (UUID id : from) {
			ServerPlayerEntity player = players.getPlayerByUUID(id);
			if (player != null) {
				selectedPlayers.add(player);
			}
		}

		// shuffle for fairness
		Collections.shuffle(selectedPlayers);

		for (ServerPlayerEntity player : selectedPlayers) {
			if (participants.size() < maximumParticipants) {
				participants.add(player);
			} else {
				spectators.add(player);
			}
		}
	}

	public void add(UUID id, @Nullable PlayerRole requestedRole) {
		remove(id);
		getSetForRole(requestedRole).add(id);
	}

	public boolean remove(UUID id) {
		return any.remove(id) || participants.remove(id) || spectators.remove(id);
	}

	public boolean contains(UUID id) {
		return any.contains(id) || participants.contains(id) || spectators.contains(id);
	}

	public int size() {
		return any.size() + participants.size() + spectators.size();
	}

	public int participantCount() {
		return any.size() + participants.size();
	}

	private Set<UUID> getSetForRole(@Nullable PlayerRole role) {
		if (role == PlayerRole.PARTICIPANT) {
			return participants;
		} else if (role == PlayerRole.SPECTATOR) {
			return spectators;
		} else {
			return any;
		}
	}
}
