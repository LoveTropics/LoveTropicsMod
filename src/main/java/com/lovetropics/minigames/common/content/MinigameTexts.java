package com.lovetropics.minigames.common.content;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;
import net.minecraft.network.chat.Component;

import java.util.function.BiConsumer;

public final class MinigameTexts {
	private static final TranslationCollector KEYS = new TranslationCollector(Constants.MODID + ".minigame.");

	public static final Component SURVIVE_THE_TIDE_1 = KEYS.add("survive_the_tide_1", "Survive The Tide I");
	public static final Component SURVIVE_THE_TIDE_1_TEAMS = KEYS.add("survive_the_tide_1_teams", "Survive The Tide I (Teams)");
	public static final Component SURVIVE_THE_TIDE_2 = KEYS.add("survive_the_tide_2", "Survive The Tide II");
	public static final Component SURVIVE_THE_TIDE_2_TEAMS = KEYS.add("survive_the_tide_2_teams", "Survive The Tide II (Teams)");
	public static final Component SURVIVE_THE_TIDE_3 = KEYS.add("survive_the_tide_3", "Survive The Tide III");
	public static final Component SURVIVE_THE_TIDE_3_TEAMS = KEYS.add("survive_the_tide_3_teams", "Survive The Tide III (Teams)");
	public static final Component SIGNATURE_RUN = KEYS.add("signature_run", "Signature Run");
	public static final Component TRASH_DIVE = KEYS.add("trash_dive", "Trash Dive");
	public static final Component CONSERVATION_EXPLORATION = KEYS.add("conservation_exploration", "Conservation Exploration");
	public static final Component TREASURE_HUNT = KEYS.add("treasure_hunt", "Treasure Hunt");
	public static final Component SPLEEF_STANDARD = KEYS.add("spleef_standard", "Spleef");
	public static final Component VOLCANO_SPLEEF = KEYS.add("volcano_spleef", "Volcano Spleef");
	public static final Component BUILD_COMPETITION = KEYS.add("build_competition", "Build Competition");
	public static final Component TURTLE_RACE = KEYS.add("turtle_race", "Turtle Race");
	public static final Component ARCADE_TURTLE_RACE = KEYS.add("arcade_turtle_race", "Arcade Turtle Race");
	public static final Component FLYING_TURTLE_RACE = KEYS.add("flying_turtle_race", "Flying Turtle Race");
	public static final Component TURTLE_SPRINT = KEYS.add("turtle_sprint", "Turtle Sprint");
	public static final Component HIDE_AND_SEEK = KEYS.add("hide_and_seek", "Hide & Seek");
	public static final Component CALAMITY = KEYS.add("calamity", "Calamity");

	public static final Component[] SURVIVE_THE_TIDE_INTRO = {
			KEYS.add("survive_the_tide_intro1", "The year...2050. Human-caused climate change has gone unmitigated and the human population has been forced to flee to higher ground."),
			KEYS.add("survive_the_tide_intro2", "\nYour task, should you choose to accept it, which you have to because of climate change, is to survive the rising tides, unpredictable weather, and other players."),
			KEYS.add("survive_the_tide_intro3", "\nBrave the conditions and defeat the others who are just trying to survive, like you. And remember...your resources are as limited as your time."),
			KEYS.add("survive_the_tide_intro4", "\nSomeone else may have the tool or food you need to survive. What kind of person will you be when the world is falling apart?"),
			KEYS.add("survive_the_tide_intro5", "\nLet's see!"),
	};
	public static final Component[] SURVIVE_THE_TIDE_FINISH = {
			KEYS.add("survive_the_tide_finish1", "Through the rising sea levels, the volatile and chaotic weather, and the struggle to survive, one player remains: %s."),
			KEYS.add("survive_the_tide_finish2", "\nThose who have fallen have been swept away by the encroaching tides that engulf countless landmasses in this dire future."),
			KEYS.add("survive_the_tide_finish3", "\nThe lone survivor of this island, %s, has won - but at what cost? The world is not what it once was, and they must survive in this new apocalyptic land."),
			KEYS.add("survive_the_tide_finish4", "\nWhat would you do different next time? Together, we could stop this from becoming our future."),
	};

	public static final Component SURVIVE_THE_TIDE_PVP_ENABLED_TITLE = KEYS.add("survive_the_tide_pvp_enabled.title", "PVP IS ENABLED!");
	public static final Component SURVIVE_THE_TIDE_PVP_ENABLED_SUBTITLE = KEYS.add("survive_the_tide_pvp_enabled.subtitle", "Beware of other players...");

	public static final Component SURVIVE_THE_TIDE_DOWN_TO_TWO = KEYS.add("survive_the_tide_down_to_two", "IT'S DOWN TO TWO PLAYERS! %s and %s are now head to head - who will triumph above these rising tides?");

	public static void collectTranslations(BiConsumer<String, String> consumer) {
		KEYS.forEach(consumer);
	}
}
