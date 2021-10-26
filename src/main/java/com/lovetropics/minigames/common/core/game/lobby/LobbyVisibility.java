package com.lovetropics.minigames.common.core.game.lobby;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public enum LobbyVisibility {
	PUBLIC(new StringTextComponent("Public")),
	PUBLIC_LIVE(new StringTextComponent("Public (Live)")),
	PRIVATE(new StringTextComponent("Private"));

	private final ITextComponent name;

	LobbyVisibility(ITextComponent name) {
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

	public ITextComponent getName() {
		return name;
	}
}
