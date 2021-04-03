package com.lovetropics.minigames.common.core.game;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
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

	public int participantCount() {
		return any.size() + participants.size();
	}

	public int spectatorCount() {
		return spectators.size();
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

	@Nullable
	@Override
	public ServerPlayerEntity getPlayerBy(UUID id) {
		return contains(id) ? server.getPlayerList().getPlayerByUUID(id) : null;
	}

	@Override
	public Iterator<ServerPlayerEntity> iterator() {
		PlayerList playerList = server.getPlayerList();
		Iterator<UUID> ids = Iterators.concat(any.iterator(), participants.iterator(), spectators.iterator());

		return new AbstractIterator<ServerPlayerEntity>() {
			@Override
			protected ServerPlayerEntity computeNext() {
				while (true) {
					if (!ids.hasNext()) {
						return endOfData();
					}

					UUID id = ids.next();
					ServerPlayerEntity player = playerList.getPlayerByUUID(id);
					if (player != null) {
						return player;
					}
				}
			}
		};
	}
}
