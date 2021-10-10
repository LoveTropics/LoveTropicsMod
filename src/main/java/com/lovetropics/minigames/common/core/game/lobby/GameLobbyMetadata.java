package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;

import javax.annotation.Nullable;

public final class GameLobbyMetadata {
	private final GameLobbyId id;
	private final PlayerKey initiator;
	private final String name;
	private final String commandId;
	private final LobbyVisibility visibility;

	public GameLobbyMetadata(GameLobbyId id, PlayerKey initiator, String name, String commandId) {
		this(id, initiator, name, commandId, LobbyVisibility.PRIVATE);
	}

	private GameLobbyMetadata(GameLobbyId id, PlayerKey initiator, String name, String commandId, LobbyVisibility visibility) {
		this.id = id;
		this.initiator = initiator;
		this.name = name;
		this.commandId = commandId;
		this.visibility = visibility;
	}

	public GameLobbyId id() {
		return this.id;
	}

	public PlayerKey initiator() {
		return this.initiator;
	}

	public String name() {
		return this.name;
	}

	public String commandId() {
		return this.commandId;
	}

	public LobbyVisibility visibility() {
		return this.visibility;
	}

	public String joinCommand(@Nullable PlayerRole role) {
		String command = "/game join " + commandId;
		if (role != null) {
			command += " as " + role.getKey();
		}
		return command;
	}

	public GameLobbyMetadata withName(String name, String commandId) {
		return new GameLobbyMetadata(this.id, this.initiator, name, commandId, this.visibility);
	}

	public GameLobbyMetadata withVisibility(LobbyVisibility visibility) {
		return new GameLobbyMetadata(this.id, this.initiator, this.name, this.commandId, visibility);
	}
}
