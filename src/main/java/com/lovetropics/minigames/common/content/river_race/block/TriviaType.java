package com.lovetropics.minigames.common.content.river_race.block;

import net.minecraft.util.StringRepresentable;

public enum TriviaType implements StringRepresentable {
	REWARD("easy"),
	GATE("medium"),
	COLLECTABLE("hard"),
	VICTORY("hard");

	private final String difficulty;

	TriviaType(String difficulty) {
		this.difficulty = difficulty;
	}

	public String difficulty() {
		return difficulty;
	}

	@Override
	public String getSerializedName() {
		return toString().toLowerCase();
	}
}
