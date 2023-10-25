package com.lovetropics.minigames.common.content;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;
import com.lovetropics.minigames.common.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class MinigameTexts {
	public static final TranslationCollector KEYS = new TranslationCollector(Constants.MODID + ".minigame.");

	public static final Component SURVIVE_THE_TIDE_1 = KEYS.add("survive_the_tide_1", "Survive The Tide I");
	public static final Component SURVIVE_THE_TIDE_1_TEAMS = KEYS.add("survive_the_tide_1_teams", "Survive The Tide I (Teams)");
	public static final Component SURVIVE_THE_TIDE_2 = KEYS.add("survive_the_tide_2", "Survive The Tide II");
	public static final Component SURVIVE_THE_TIDE_2_TEAMS = KEYS.add("survive_the_tide_2_teams", "Survive The Tide II (Teams)");
	public static final Component SURVIVE_THE_TIDE_3 = KEYS.add("survive_the_tide_3", "Survive The Tide III");
	public static final Component SURVIVE_THE_TIDE_3_TEAMS = KEYS.add("survive_the_tide_3_teams", "Survive The Tide III (Teams)");
	public static final Component SURVIVE_THE_TIDE_4 = KEYS.add("survive_the_tide_4", "Survive The Tide IV");
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
	public static final Component BLOCK_PARTY = KEYS.add("block_party", "Block Party");
	public static final Component CHAOS_BLOCK_PARTY = KEYS.add("chaos_block_party", "Chaos Block Party");
	public static final Component LEVITATION = KEYS.add("levitation", "Levitation");

	// TODO: These should move into SurviveTheTideTexts
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

	public static final TranslationCollector.Fun1 PLAYER_WON = KEYS.add1("player_won", "\u2B50 %s won the game!");
	public static final Component NOBODY_WON = KEYS.add("nobody_won", "\u2B50 Nobody won the game!");
	public static final Component RESULTS = KEYS.add("results", "The game is over! Here are the results:");

	public static final TranslationCollector.Fun1 JOIN_TEAM = KEYS.add1("teams.join", "Join %s");
	public static final TranslationCollector.Fun1 JOINED_TEAM = KEYS.add1("teams.joined", "You have requested to join: %s").withStyle(ChatFormatting.GRAY);
	public static final TranslationCollector.Fun1 ON_TEAM = KEYS.add1("teams.on_team", "You are on %s team!").withStyle(ChatFormatting.GRAY);
	public static final Component[] TEAMS_INTRO = {
			KEYS.add("teams.intro_1", "This is a team-based game!").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
			KEYS.add("teams.intro_2", "You can select a team preference by using the items in your inventory:").withStyle(ChatFormatting.GRAY, ChatFormatting.BOLD)
	};
	public static final Component TEAM_RED = KEYS.add("teams.red", "Red");
	public static final Component TEAM_BLUE = KEYS.add("teams.blue", "Blue");
	public static final Component TEAM_YELLOW = KEYS.add("teams.yellow", "Yellow");
	public static final Component TEAM_GREEN = KEYS.add("teams.green", "Green");
	public static final Component TEAM_HIDERS = KEYS.add("teams.hiders", "Hiders");
	public static final Component TEAM_SEEKERS = KEYS.add("teams.seekers", "Seekers");

	public static final Component CLEAR_WEATHER = KEYS.add("weather.clear", "Clear");
	public static final Component HEAVY_RAIN = KEYS.add("weather.heavy_rain", "Heavy Rain").withStyle(ChatFormatting.BLUE);
	public static final Component ACID_RAIN = KEYS.add("weather.acid_rain", "Acid Rain").withStyle(ChatFormatting.GREEN);
	public static final Component HAIL = KEYS.add("weather.hail", "Hail").withStyle(ChatFormatting.BLUE);
	public static final Component HEATWAVE = KEYS.add("weather.heatwave", "Heatwave").withStyle(ChatFormatting.YELLOW);
	public static final Component SANDSTORM = KEYS.add("weather.sandstorm", "Sandstorm").withStyle(ChatFormatting.YELLOW);
	public static final Component SNOWSTORM = KEYS.add("weather.snowstorm", "Snowstorm").withStyle(ChatFormatting.WHITE);

	public static final Component SPECTATING_NOTIFICATION = KEYS.add2("spectating_notification", "You are a %s!\nScroll or use the arrow keys to select players.\nHold %s and scroll to zoom.").apply(
			KEYS.add("spectating_notification.spectator", "spectator").withStyle(ChatFormatting.BOLD),
			KEYS.add("spectating_notification.key", "Left Control").withStyle(ChatFormatting.UNDERLINE)
	);
	private static final TranslationCollector.Fun2 PROGRESS_BAR_TIME = KEYS.add2("progress_bar.time", "%s (%s left)").withStyle(ChatFormatting.GRAY);

	public static Component progressBarTime(Component text, int secondsLeft) {
		return PROGRESS_BAR_TIME.apply(text, Util.formatMinutesSeconds(secondsLeft));
	}

	public static final Component UNKNOWN_DONOR = KEYS.add("donation.unknown_donor", "an unknown donor").withStyle(ChatFormatting.BLUE);
	public static final Component EVERYONE_RECEIVER = KEYS.add("donation.everyone_receiver", "Everyone").withStyle(ChatFormatting.BLUE);
	public static final TranslationCollector.Fun1 PACKAGE_RECEIVED = KEYS.add1("donation.package_received", "%s received a package!");

	public static final Component REWARDS = KEYS.add("rewards_granted", "You got rewards for playing minigames!").withStyle(ChatFormatting.GOLD);
	public static final TranslationCollector.Fun2 REWARD_ITEM = KEYS.add2("reward_item", " - %sx %s").withStyle(ChatFormatting.GRAY);

	static {
		KEYS.add("starting_in", "Starting in %time%!");
		KEYS.add("donation.swap", "%sender% is swapping everyone with a nearby player!");
		KEYS.add("donation.swap.warning", "You will be swapping with a nearby player in %time% seconds!");
		KEYS.add("donation.hunger_sabotage", "%sender% sent you a HUNGER SABOTAGE for 30 seconds!");
		KEYS.add("donation.helpful_effects", "%sender% sent you helpful effects for 1 minute!");
		KEYS.add("donation.tapir_party", "Everyone is a Tapir for 2 minutes!");
		KEYS.add("donation.acid_rain", "It is raining acid for 1 minute!");

		KEYS.add("levitation.intro1", "Race to be the first to reach the top of the tube!");
		KEYS.add("levitation.intro2", "Use your fishing rod to pull other players down.");
	}
}
