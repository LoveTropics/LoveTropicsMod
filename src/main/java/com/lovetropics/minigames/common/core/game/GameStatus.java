package com.lovetropics.minigames.common.core.game;

import net.minecraft.util.text.TextFormatting;

public enum GameStatus {
	POLLING("waiting for players", TextFormatting.GOLD),
	ACTIVE("in progress", TextFormatting.GREEN);

	public final String description;
	public final TextFormatting color;

	GameStatus(String description, TextFormatting color) {
		this.description = description;
		this.color = color;
	}
}
