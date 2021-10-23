package com.lovetropics.minigames.common.content;

import com.lovetropics.minigames.Constants;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.BiConsumer;

public final class MinigameTexts {
	public static void collectTranslations(BiConsumer<String, String> consumer) {
		Keys.collectTranslations(consumer);
	}

	static final class Keys {
		static final String SURVIVE_THE_TIDE_1 = key("survive_the_tide_1");
		static final String SURVIVE_THE_TIDE_1_TEAMS = key("survive_the_tide_1_teams");
		static final String SURVIVE_THE_TIDE_2 = key("survive_the_tide_2");
		static final String SURVIVE_THE_TIDE_2_TEAMS = key("survive_the_tide_2_teams");
		static final String SURVIVE_THE_TIDE_3 = key("survive_the_tide_3");
		static final String SURVIVE_THE_TIDE_3_TEAMS = key("survive_the_tide_3_teams");
		static final String SIGNATURE_RUN = key("signature_run");
		static final String TRASH_DIVE = key("trash_dive");
		static final String CONSERVATION_EXPLORATION = key("conservation_exploration");
		static final String TREASURE_HUNT = key("treasure_hunt");
		static final String VOLCANO_SPLEEF = key("volcano_spleef");
		static final String BUILD_COMPETITION = key("build_competition");
		static final String TURTLE_RACE = key("turtle_race");
		static final String ARCADE_TURTLE_RACE = key("arcade_turtle_race");
		static final String FLYING_TURTLE_RACE = key("flying_turtle_race");
		static final String TURTLE_SPRINT = key("turtle_sprint");
		static final String HIDE_AND_SEEK = key("hide_and_seek");

		static final String MANGROVES_AND_PIANGUAS = key("mangroves_and_pianguas");
		static final String MP_WAVE_WARNING = key("mp_wave_warning");
		static final String MP_DEATH_DECREASE = key("mp_death_decrease");
		static final String MP_CURRENCY_ADDITION = key("mp_currency_addition");
		static final String MP_TRADING = key("mp_trading");
		static final String MP_CAN_ONLY_PLACE_PLANTS = key("mp_can_only_place_plants");
		static final String MP_PLANT_CANNOT_FIT = key("mp_plant_cannot_fit");
		static final String MP_NOT_YOUR_PLOT = key("mp_not_your_plot");

		static final String[] SURVIVE_THE_TIDE_INTRO = new String[] {
				key("survive_the_tide_intro1"),
				key("survive_the_tide_intro2"),
				key("survive_the_tide_intro3"),
				key("survive_the_tide_intro4"),
				key("survive_the_tide_intro5")
		};
		static final String[] SURVIVE_THE_TIDE_FINISH = new String[] {
				key("survive_the_tide_finish1"),
				key("survive_the_tide_finish2"),
				key("survive_the_tide_finish3"),
				key("survive_the_tide_finish4")
		};

		static final String SURVIVE_THE_TIDE_PVP_DISABLED = key("survive_the_tide_pvp_disabled");
		static final String SURVIVE_THE_TIDE_PVP_ENABLED = key("survive_the_tide_pvp_enabled");
		static final String SURVIVE_THE_TIDE_DOWN_TO_TWO = key("survive_the_tide_down_to_two");

		static void collectTranslations(BiConsumer<String, String> consumer) {
			consumer.accept(SURVIVE_THE_TIDE_1, "Survive The Tide I");
			consumer.accept(SURVIVE_THE_TIDE_1_TEAMS, "Survive The Tide I (Teams)");
			consumer.accept(SURVIVE_THE_TIDE_2, "Survive The Tide II");
			consumer.accept(SURVIVE_THE_TIDE_2_TEAMS, "Survive The Tide II (Teams)");
			consumer.accept(SURVIVE_THE_TIDE_3, "Survive The Tide III");
			consumer.accept(SURVIVE_THE_TIDE_3_TEAMS, "Survive The Tide III (Teams)");
			consumer.accept(SIGNATURE_RUN, "Signature Run");
			consumer.accept(TRASH_DIVE, "Trash Dive");
			consumer.accept(CONSERVATION_EXPLORATION, "Conservation Exploration");
			consumer.accept(TREASURE_HUNT, "Treasure Hunt");
			consumer.accept(VOLCANO_SPLEEF, "Volcano Spleef");
			consumer.accept(BUILD_COMPETITION, "Build Competition");
			consumer.accept(TURTLE_RACE, "Turtle Race");
			consumer.accept(ARCADE_TURTLE_RACE, "Arcade Turtle Race");
			consumer.accept(FLYING_TURTLE_RACE, "Flying Turtle Race");
			consumer.accept(TURTLE_SPRINT, "Turtle Sprint");
			consumer.accept(HIDE_AND_SEEK, "Hide & Seek");

			consumer.accept(MANGROVES_AND_PIANGUAS, "Mangroves And Pianguas");
			consumer.accept(MP_WAVE_WARNING, "A wave is coming soon!");
			consumer.accept(MP_TRADING, "Trading");
			consumer.accept(MP_DEATH_DECREASE, "You died and lost %s currency!");
			consumer.accept(MP_CURRENCY_ADDITION, "You gained %s currency!");
			consumer.accept(MP_CAN_ONLY_PLACE_PLANTS, "You can only place plants you got from the shop!");
			consumer.accept(MP_PLANT_CANNOT_FIT, "This plant cannot fit here!");
			consumer.accept(MP_NOT_YOUR_PLOT, "This is not your plot, you cannot edit here!");

			consumer.accept(SURVIVE_THE_TIDE_INTRO[0], "The year...2050. Human-caused climate change has gone unmitigated and the human population has been forced to flee to higher ground.");
			consumer.accept(SURVIVE_THE_TIDE_INTRO[1], "\nYour task, should you choose to accept it, which you have to because of climate change, is to survive the rising tides, unpredictable weather, and other players.");
			consumer.accept(SURVIVE_THE_TIDE_INTRO[2], "\nBrave the conditions and defeat the others who are just trying to survive, like you. And remember...your resources are as limited as your time.");
			consumer.accept(SURVIVE_THE_TIDE_INTRO[3], "\nSomeone else may have the tool or food you need to survive. What kind of person will you be when the world is falling apart?");
			consumer.accept(SURVIVE_THE_TIDE_INTRO[4], "\nLet's see!");

			consumer.accept(SURVIVE_THE_TIDE_FINISH[0], "Through the rising sea levels, the volatile and chaotic weather, and the struggle to survive, one player remains: %s.");
			consumer.accept(SURVIVE_THE_TIDE_FINISH[1], "\nThose who have fallen have been swept away by the encroaching tides that engulf countless landmasses in this dire future.");
			consumer.accept(SURVIVE_THE_TIDE_FINISH[2], "\nThe lone survivor of this island, %s, has won - but at what cost? The world is not what it once was, and they must survive in this new apocalyptic land.");
			consumer.accept(SURVIVE_THE_TIDE_FINISH[3], "\nWhat would you do different next time? Together, we could stop this from becoming our future.");

			consumer.accept(SURVIVE_THE_TIDE_PVP_DISABLED, "NOTE: PvP is disabled for %s minutes! Go fetch resources before time runs out.");
			consumer.accept(SURVIVE_THE_TIDE_PVP_ENABLED, "WARNING: PVP HAS BEEN ENABLED! Beware of other players...");

			consumer.accept(SURVIVE_THE_TIDE_DOWN_TO_TWO, "IT'S DOWN TO TWO PLAYERS! %s and %s are now head to head - who will triumph above these rising tides?");
		}

		static String key(String key) {
			return Constants.MODID + ".minigame." + key;
		}
	}

	public static IFormattableTextComponent mpWaveWarning() {
		return new TranslationTextComponent(Keys.MP_WAVE_WARNING);
	}

	public static IFormattableTextComponent mpDeathDecrease(int count) {
		return new TranslationTextComponent(Keys.MP_DEATH_DECREASE, count);
	}

	public static IFormattableTextComponent mpCurrencyAddition(int amount) {
		return new TranslationTextComponent(Keys.MP_CURRENCY_ADDITION, amount);
	}

	public static IFormattableTextComponent mpTrading() {
		return new TranslationTextComponent(Keys.MP_TRADING);
	}

	public static IFormattableTextComponent mpCanOnlyPlacePlants() {
		return new TranslationTextComponent(Keys.MP_CAN_ONLY_PLACE_PLANTS);
	}

	public static IFormattableTextComponent mpPlantCannotFit() {
		return new TranslationTextComponent(Keys.MP_PLANT_CANNOT_FIT);
	}

	public static IFormattableTextComponent mpNotYourPlot() {
		return new TranslationTextComponent(Keys.MP_NOT_YOUR_PLOT);
	}
}
