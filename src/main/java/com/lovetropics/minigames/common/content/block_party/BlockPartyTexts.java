package com.lovetropics.minigames.common.content.block_party;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class BlockPartyTexts {
	public static final TranslationCollector KEYS = new TranslationCollector(LoveTropics.ID + ".minigame.block_party.");

	public static final TranslationCollector.Fun1 STAND_ON_BLOCK = KEYS.add1("stand_on_block", "Stand on %s");
	public static final TranslationCollector.Fun1 BREAK_IN_SECONDS = KEYS.add1("break_in_seconds", "Break in %s seconds");
	public static final Component KNOCKBACK_ENABLED = KEYS.add("knockback_enabled", "Knockback has been enabled - look out!").withStyle(ChatFormatting.RED);
}
