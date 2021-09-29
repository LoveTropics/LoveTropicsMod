package com.lovetropics.minigames.common.core.game.lobby;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public enum LobbyVisibility {
	PUBLIC(new StringTextComponent("Public")),
	PRIVATE(new StringTextComponent("Private"));

	private final ITextComponent name;

	LobbyVisibility(ITextComponent name) {
		this.name = name;
	}

	public boolean isPublic() {
		return this == PUBLIC;
	}

	public boolean isPrivate() {
		return this == PRIVATE;
	}

	public ITextComponent getName() {
		return name;
	}
}
