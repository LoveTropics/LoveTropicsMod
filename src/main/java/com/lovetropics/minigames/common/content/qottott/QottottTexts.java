package com.lovetropics.minigames.common.content.qottott;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;

public class QottottTexts {
	public static final TranslationCollector KEYS = new TranslationCollector(Constants.MODID + ".minigame.qottott.");

	static {
		KEYS.add("sidebar.title", "Qottot");
		KEYS.add("sidebar.instruction", "Pick up points!");
		KEYS.add("sidebar.top_players", "Top Players:");
		KEYS.add("time_remaining", "Time Remaining: %time%...");
	}
}
