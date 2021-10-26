package com.lovetropics.minigames.client.lobby.state;

import com.lovetropics.minigames.common.core.game.LobbyStatus;
import com.lovetropics.minigames.common.core.game.player.PlayerRole;

import javax.annotation.Nullable;

public class ClientLobbyState {
	final int id;

	String name;
	int participantCount;
	int spectatorCount;

	@Nullable
	ClientCurrentGame currentGame;

	@Nullable
	PlayerRole joinedRole;

	ClientLobbyState(int id) {
		this.id = id;
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

	public void setPlayerCounts(int participantCount, int spectatorCount) {
		this.participantCount = participantCount;
		this.spectatorCount = spectatorCount;
	}

	public int getParticipantCount() {
		return participantCount;
	}

	public int getSpectatorCount() {
		return spectatorCount;
	}

	public int getPlayerCount(PlayerRole role) {
		return role == PlayerRole.PARTICIPANT ? participantCount : spectatorCount;
	}

	public int getPlayerCount() {
		return participantCount + spectatorCount;
	}

	@Nullable
	public PlayerRole getJoinedRole() {
		return joinedRole;
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
