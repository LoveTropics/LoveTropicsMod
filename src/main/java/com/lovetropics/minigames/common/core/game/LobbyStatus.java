package com.lovetropics.minigames.common.core.game;

import net.minecraft.util.text.TextFormatting;

public enum LobbyStatus {
	WAITING("waiting", TextFormatting.GOLD),
	PLAYING("in progress", TextFormatting.GREEN),
	PAUSED("paused", TextFormatting.RED);

	public final String description;
	public final TextFormatting color;

	LobbyStatus(String description, TextFormatting color) {
		this.description = description;
		this.color = color;
	}
}
