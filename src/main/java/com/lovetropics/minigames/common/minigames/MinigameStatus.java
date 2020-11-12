package com.lovetropics.minigames.common.minigames;

import net.minecraft.util.text.TextFormatting;

public enum MinigameStatus {
	POLLING("waiting for players", TextFormatting.GOLD),
	ACTIVE("in progress", TextFormatting.GREEN);

	public final String description;
	public final TextFormatting color;

	MinigameStatus(String description, TextFormatting color) {
		this.description = description;
		this.color = color;
	}
}
