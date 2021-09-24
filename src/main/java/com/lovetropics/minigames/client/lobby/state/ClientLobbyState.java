package com.lovetropics.minigames.client.lobby.state;

import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import javax.annotation.Nullable;
import java.util.*;

public class ClientLobbyState {
	final int id;

	String name;
	final Map<UUID, ClientLobbyPlayerEntry> players = new Object2ObjectOpenHashMap<>();

	List<ClientQueuedGame> queue = new ArrayList<>();

	@Nullable
	ClientGameDefinition activeGame;

	@Nullable
	PlayerRole joinedRole;

	ClientLobbyState(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public List<ClientQueuedGame> getQueue() {
		return queue;
	}

	@Nullable
	public ClientGameDefinition getActiveGame() {
		return activeGame;
	}

	public void addPlayers(List<ClientLobbyPlayerEntry> players) {
		for (ClientLobbyPlayerEntry player : players) {
			this.players.put(player.uuid(), player);
		}
	}

	// TODO: sending full metadata for removing?
	public void removePlayers(List<ClientLobbyPlayerEntry> players) {
		for (ClientLobbyPlayerEntry player : players) {
			this.players.remove(player.uuid());
		}
	}

	public void setPlayers(List<ClientLobbyPlayerEntry> players) {
		this.players.clear();
		this.addPlayers(players);
	}

	public Collection<ClientLobbyPlayerEntry> getPlayers() {
		return this.players.values();
	}
}
