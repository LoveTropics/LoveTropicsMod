package com.lovetropics.minigames.common.content.trash_dive;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;

public class TrashDiveTexts {
	public static final TranslationCollector KEYS = new TranslationCollector(LoveTropics.ID + ".minigame.trash_dive.");

	static {
		KEYS.add("sidebar.title", "Trash Dive");
		KEYS.add("sidebar.instruction", "Pick up trash!");
		KEYS.add1("sidebar.collected", "%total% collected");
		KEYS.add("sidebar.top_players", "MVPs:");
		KEYS.add("time_remaining", "Time Remaining: %time%...");
	}
}
