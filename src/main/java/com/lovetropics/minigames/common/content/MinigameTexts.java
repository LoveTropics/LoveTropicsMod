package com.lovetropics.minigames.common.content;

import com.lovetropics.minigames.Constants;

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
		static final String SPLEEF_STANDARD = key("spleef_standard");
		static final String VOLCANO_SPLEEF = key("volcano_spleef");
		static final String BUILD_COMPETITION = key("build_competition");
		static final String TURTLE_RACE = key("turtle_race");
		static final String ARCADE_TURTLE_RACE = key("arcade_turtle_race");
		static final String FLYING_TURTLE_RACE = key("flying_turtle_race");
		static final String TURTLE_SPRINT = key("turtle_sprint");
		static final String HIDE_AND_SEEK = key("hide_and_seek");
		static final String CALAMITY = key("calamity");

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
			consumer.accept(SPLEEF_STANDARD, "Spleef");
			consumer.accept(VOLCANO_SPLEEF, "Volcano Spleef");
			consumer.accept(BUILD_COMPETITION, "Build Competition");
			consumer.accept(TURTLE_RACE, "Turtle Race");
			consumer.accept(ARCADE_TURTLE_RACE, "Arcade Turtle Race");
			consumer.accept(FLYING_TURTLE_RACE, "Flying Turtle Race");
			consumer.accept(TURTLE_SPRINT, "Turtle Sprint");
			consumer.accept(HIDE_AND_SEEK, "Hide & Seek");
			consumer.accept(CALAMITY, "Calamity");

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
}
