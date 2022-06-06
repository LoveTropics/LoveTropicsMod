package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.player.PlayerRole;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;

import javax.annotation.Nullable;

public record GameLobbyMetadata(GameLobbyId id, PlayerKey initiator, String name, String commandId, LobbyVisibility visibility) {
	public GameLobbyMetadata(GameLobbyId id, PlayerKey initiator, String name, String commandId) {
		this(id, initiator, name, commandId, LobbyVisibility.PRIVATE);
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
