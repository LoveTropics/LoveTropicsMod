package com.lovetropics.minigames.common.core.game.lobby;

import com.lovetropics.minigames.common.core.game.util.GameTexts;
import net.minecraft.network.chat.Component;

public enum LobbyVisibility {
	PUBLIC(GameTexts.Ui.LOBBY_PUBLIC),
	PUBLIC_LIVE(GameTexts.Ui.LOBBY_PUBLIC_LIVE),
	PRIVATE(GameTexts.Ui.LOBBY_PRIVATE);

	private final Component name;

	LobbyVisibility(Component name) {
		this.name = name;
	}

	public boolean isPublic() {
		return !this.isPrivate();
	}

	public boolean isPrivate() {
		return this == PRIVATE;
	}

	public boolean isFocusedLive() {
		return this == PUBLIC_LIVE;
	}

	public Component getName() {
		return name;
	}
}
