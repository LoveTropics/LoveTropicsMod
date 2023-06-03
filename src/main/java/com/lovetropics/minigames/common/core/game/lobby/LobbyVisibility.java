package com.lovetropics.minigames.common.core.game.lobby;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;

public enum LobbyVisibility {
	PUBLIC(Component.literal("Public")),
	PUBLIC_LIVE(Component.literal("Public (Live)")),
	PRIVATE(Component.literal("Private"));

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
