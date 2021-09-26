package com.lovetropics.minigames.common.core.game;

import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.lovetropics.minigames.common.core.game.player.PlayerIterable;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;

import javax.annotation.Nullable;
import java.util.*;

public final class GameRegistrations implements PlayerSet {
	private final MinecraftServer server;

	private final Set<UUID> any = new ObjectOpenHashSet<>();
	private final Set<UUID> participants = new ObjectOpenHashSet<>();
	private final Set<UUID> spectators = new ObjectOpenHashSet<>();

	public GameRegistrations(MinecraftServer server) {
		this.server = server;
	}

	public void clear() {
		this.any.clear();
		this.participants.clear();
		this.spectators.clear();
	}

	public void collectInto(
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

	public boolean add(UUID id, @Nullable PlayerRole requestedRole) {
		if (contains(id)) {
			getSetForRequestedRole(requestedRole).add(id);
			return true;
		} else {
			return false;
		}
	}

	private Set<UUID> getSetForRequestedRole(@Nullable PlayerRole role) {
		if (role == PlayerRole.PARTICIPANT) {
			return participants;
		} else if (role == PlayerRole.SPECTATOR) {
			return spectators;
		} else {
			return any;
		}
	}

	public boolean remove(UUID id) {
		return any.remove(id) | participants.remove(id) | spectators.remove(id);
	}

	@Override
	public boolean contains(UUID id) {
		return any.contains(id) || participants.contains(id) || spectators.contains(id);
	}

	@Override
	public int size() {
		return any.size() + participants.size() + spectators.size();
	}

	public PlayerSet getPlayersWithRole(PlayerRole role) {
		return role == PlayerRole.PARTICIPANT ? this.getParticipants() : this.getSpectators();
	}

	@Nullable
	public PlayerRole getRoleFor(UUID id) {
		if (any.contains(id) || participants.contains(id)) {
			return PlayerRole.PARTICIPANT;
		} else if (spectators.contains(id)) {
			return PlayerRole.SPECTATOR;
		} else {
			return null;
		}
	}

	public PlayerSet getParticipants() {
		return PlayerSet.wrap(server, Sets.union(participants, any));
	}

	public PlayerSet getSpectators() {
		return PlayerSet.wrap(server, spectators);
	}

	@Nullable
	@Override
	public ServerPlayerEntity getPlayerBy(UUID id) {
		return contains(id) ? server.getPlayerList().getPlayerByUUID(id) : null;
	}

	@Override
	public Iterator<ServerPlayerEntity> iterator() {
		Iterator<UUID> ids = Iterators.concat(any.iterator(), participants.iterator(), spectators.iterator());
		return PlayerIterable.resolvingIterator(server, ids);
	}
}
