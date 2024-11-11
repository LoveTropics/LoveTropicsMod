package com.lovetropics.minigames.common.content.river_race;

import com.lovetropics.minigames.LoveTropics;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;

public final class RiverRaceTexts {
	private static final TranslationCollector KEYS = new TranslationCollector(LoveTropics.ID + ".minigame.river_race.");

	public static final Component SHOP = KEYS.add("shop", "Shop");
	public static final Component CORRECT_ANSWER = KEYS.add("trivia.correct", "Trivia question answered correctly!")
			.withStyle(ChatFormatting.GREEN);
	public static final TranslationCollector.Fun1 INCORRECT_ANSWER = KEYS.add1("trivia.incorrect", "Incorrect! This question is now locked out for %s seconds!")
			.withStyle(ChatFormatting.RED);

	public static final Component COLLECTABLE_GIVEN = KEYS.add("trivia.collectable_given", "You've been given a collectable to place into the monument!")
			.withStyle(ChatFormatting.GOLD);
	public static final Component VICTORY_POINT_GIVEN = KEYS.add("trivia.victory_point_given", "You've gained a victory point!")
			.withStyle(ChatFormatting.GOLD);
	public static final Component LOOT_GIVEN = KEYS.add("trivia.loot_given", "Loot has been unlocked!")
			.withStyle(ChatFormatting.GOLD);
	public static final TranslationCollector.Fun2 COLLECTABLE_PLACED_TITLE = KEYS.add2("trivia.collectable_placed_title", "%s team are first to place the %s collectable!")
			.withStyle(ChatFormatting.GREEN);
	public static final TranslationCollector.Fun3 COLLECTABLE_PLACED = KEYS.add3("trivia.collectable_placed", "%s team are first to place the %s collectable! The micro-games will start in %s seconds.")
			.withStyle(ChatFormatting.GREEN);
	public static final TranslationCollector.Fun1 VICTORY_POINT_CHANGE = KEYS.add1("trivia.victory_point_change", "+%s Victory Point(s)")
			.withStyle(ChatFormatting.GREEN);

	public static final TranslationCollector.Fun1 GAMES_START_IN = KEYS.add1("trivia.games_start_in", "Microgames start in %s second(s)")
			.withStyle(ChatFormatting.GREEN);

	public static void collectTranslations(BiConsumer<String, String> consumer) {
		KEYS.forEach(consumer);

		consumer.accept(LoveTropics.ID + ".minigame.river_race", "River Race");
	}
}
