package com.lovetropics.minigames.common.content.crafting_bee;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class CraftingBeeTexts {
	public static final TranslationCollector KEYS = new TranslationCollector(LoveTropics.ID + ".minigame.crafting_bee.");

	public static final TranslationCollector.Fun3 TEAM_HAS_COMPLETED_RECIPES = KEYS.add3("team_has_completed_recipes", "Team %s has completed %s out of %s recipes");
	public static final Component DONT_CHEAT = KEYS.add("dont_cheat", "Don't cheat!").withStyle(ChatFormatting.RED);
	public static final Component HINT = MinigameTexts.KEYS.add("hint", "Click to show a hint, displaying the position of a random number of ingredients");
	public static final TranslationCollector.Fun1 HINTS_LEFT = MinigameTexts.KEYS.add1("hints_left", "You have %s hints left");
}
