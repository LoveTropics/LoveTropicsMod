package com.lovetropics.minigames.client.lobby.manage.state;

import com.google.common.collect.ImmutableList;
import com.lovetropics.minigames.client.lobby.state.ClientCurrentGame;
import com.lovetropics.minigames.client.lobby.state.ClientGameDefinition;
import com.lovetropics.minigames.common.core.game.lobby.LobbyControls;
import com.lovetropics.minigames.common.core.game.lobby.LobbyVisibility;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;

import javax.annotation.Nullable;
import java.util.List;

public final class ClientLobbyManageState {
	private String name = "";
	private ClientCurrentGame currentGame;
	private ClientLobbyQueue queue = new ClientLobbyQueue();
	private List<ClientLobbyPlayer> players = ImmutableList.of();
	private LobbyControls.State controlsState = LobbyControls.State.disabled();
	private LobbyVisibility visibility = LobbyVisibility.PRIVATE;

	private List<ClientGameDefinition> installedGames = ImmutableList.of();

	public String getName() {
		return name;
	}

	@Nullable
	public ClientCurrentGame getCurrentGame() {
		return currentGame;
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

	public LobbyVisibility getVisibility() {
		return visibility;
	}

	public List<ClientGameDefinition> getInstalledGames() {
		return installedGames;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCurrentGame(@Nullable ClientCurrentGame game) {
		this.currentGame = game;
	}

	public void setQueue(ClientLobbyQueue queue) {
		this.queue = queue;
	}

	public void setInstalledGames(List<ClientGameDefinition> installedGames) {
		this.installedGames = installedGames;
	}

	public void updateQueue(IntList queue, Int2ObjectMap<ClientLobbyQueuedGame> updated) {
		this.queue.applyUpdates(queue, updated);
	}

	public void setPlayers(List<ClientLobbyPlayer> players) {
		this.players = players;
	}

	public void setControlsState(LobbyControls.State state) {
		this.controlsState = state;
	}

	public void setVisibility(LobbyVisibility visibility) {
		this.visibility = visibility;
	}
}
