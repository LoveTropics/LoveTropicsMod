package com.lovetropics.minigames.common.core.game.lobby;

public enum LobbyVisibility {
	PUBLIC,
	PRIVATE;

	public boolean isPublic() {
		return this == PUBLIC;
	}

	public boolean isPrivate() {
		return this == PRIVATE;
	}
}
