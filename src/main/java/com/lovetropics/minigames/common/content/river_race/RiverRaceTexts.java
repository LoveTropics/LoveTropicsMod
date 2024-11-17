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
	public static final TranslationCollector.Fun1 VICTORY_POINT_CHANGE = KEYS.add1("trivia.victory_point_change", "+%s Victory Point(s)")
			.withStyle(ChatFormatting.GREEN);

	public static final Component CANT_PLACE_COLLECTABLE = KEYS.add("cant_place_collectable", "Place in the Monument at the end of the correct zone to progress")
			.withStyle(ChatFormatting.RED);
	public static final Component TRIVIA_BLOCK_ALREADY_USED = KEYS.add("trivia_block_already_used", "This Trivia Block has already been used!")
			.withStyle(ChatFormatting.RED);
	public static final Component YOU_HAVE_COLLECTABLE = KEYS.add("you_have_collectable", "You already have this collectable!")
			.withStyle(ChatFormatting.RED);
	public static final TranslationCollector.Fun1 PLAYER_HAS_COLLECTABLE = KEYS.add1("player_has_collectable", "%s already has this collectable!")
			.withStyle(ChatFormatting.RED);

	public static final Component MICROGAME_RESULTS = KEYS.add("microgames_results", "Microgames have completed! Here are the results:")
			.withStyle(ChatFormatting.GOLD);

	public static final TranslationCollector.Fun1 COLLECTABLE_NAME = KEYS.add1("collectable_name", "Collectable - %s");

	public static void collectTranslations(BiConsumer<String, String> consumer) {
		KEYS.add("trivia.collectable_placed.title", "Go %team%!");
		KEYS.add("trivia.collectable_placed.subtitle", "Completed %name% zone");
		KEYS.add("trivia.games_start_in", "Microgames start in %time%");

		KEYS.forEach(consumer);
		consumer.accept(LoveTropics.ID + ".minigame.river_race", "River Race");
	}
}
