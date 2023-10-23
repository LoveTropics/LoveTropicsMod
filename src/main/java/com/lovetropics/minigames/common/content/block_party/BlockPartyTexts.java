package com.lovetropics.minigames.common.content.block_party;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;

public final class BlockPartyTexts {
	public static final TranslationCollector KEYS = new TranslationCollector(Constants.MODID + ".minigame.block_party.");

	public static final TranslationCollector.Fun1 STAND_ON_BLOCK = KEYS.add1("stand_on_block", "Stand on %s");
	public static final TranslationCollector.Fun1 BREAK_IN_SECONDS = KEYS.add1("break_in_seconds", "Break in %s seconds");
}
