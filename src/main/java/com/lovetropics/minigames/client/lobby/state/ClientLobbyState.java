package com.lovetropics.minigames.client.lobby.state;

import com.lovetropics.minigames.common.core.game.LobbyStatus;

import javax.annotation.Nullable;

public class ClientLobbyState {
	final int id;

	String name;
	int playerCount;

	@Nullable
	ClientCurrentGame currentGame;

	ClientLobbyState(int id) {
		this.id = id;
	}

	public void update(String name, ClientCurrentGame currentGame) {
		this.name = name;
		this.currentGame = currentGame;
	}

	public void setPlayerCounts(int count) {
		this.playerCount = count;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	@Nullable
	public ClientCurrentGame getCurrentGame() {
		return currentGame;
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public LobbyStatus getStatus() {
		if (currentGame != null) {
			switch (currentGame.phase()) {
				case PLAYING: return LobbyStatus.PLAYING;
				case WAITING: return LobbyStatus.WAITING;
			}
		}
		return LobbyStatus.PAUSED;
	}
}
