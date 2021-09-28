package com.lovetropics.minigames.client.lobby.manage.state;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.List;

public final class ClientLobbyManageState {
	private String name = "";
	private final ClientLobbyQueue queue = new ClientLobbyQueue();
	private final List<ClientLobbyPlayer> players = new ArrayList<>();
	private final LobbyControls.State controlsState = LobbyControls.State.disabled();

	private List<ClientGameDefinition> installedGames = ImmutableList.of();

	public String getName() {
		return name;
	}

	public ClientLobbyQueue getQueue() {
		return queue;
	}

	public List<ClientLobbyPlayer> getPlayers() {
		return players;
	}

	public LobbyControls.State getControlsState() {
		return controlsState;
	}

	public List<ClientGameDefinition> getInstalledGames() {
		return installedGames;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setQueue(ClientLobbyQueue queue) {
		this.queue.clear();
		this.queue.addAll(queue);
	}

	public void setInstalledGames(List<ClientGameDefinition> installedGames) {
		this.installedGames = installedGames;
	}

	public void updateQueue(IntList queue, Int2ObjectMap<ClientLobbyQueuedGame> updated) {
		this.queue.applyUpdates(queue, updated);
	}

	public void setPlayers(List<ClientLobbyPlayer> players) {
		this.players.clear();
		this.players.addAll(players);
	}

	public void setControlsState(LobbyControls.State state) {
		this.controlsState.setFrom(state);
	}
}
