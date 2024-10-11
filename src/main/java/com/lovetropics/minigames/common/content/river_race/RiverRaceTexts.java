package com.lovetropics.minigames.common.content.river_race;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;

import java.util.function.BiConsumer;

public final class RiverRaceTexts {
	private static final TranslationCollector KEYS = new TranslationCollector(LoveTropics.ID + ".minigame.river_race.");

	static {

	}

	public static void collectTranslations(BiConsumer<String, String> consumer) {
		KEYS.forEach(consumer);

		consumer.accept(LoveTropics.ID + ".minigame.river_race", "River Race");
	}
}
