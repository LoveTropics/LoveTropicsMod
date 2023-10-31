package com.lovetropics.minigames.common.content;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.core.game.behavior.instances.donation.DonationPackageData;
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
	public static final Component QOTTOTT = KEYS.add("qottott", "Qottott");

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
	private static final TranslationCollector.Fun2 PROGRESS_BAR_TIME = KEYS.add2("progress_bar.time", "%s (%s left)");

	public static Component progressBarTime(Component text, int secondsLeft) {
		return PROGRESS_BAR_TIME.apply(text, Util.formatMinutesSeconds(secondsLeft));
	}

	public static final Component UNKNOWN_DONOR = KEYS.add("donation.unknown_donor", "an unknown donor").withStyle(ChatFormatting.BLUE);
	public static final Component EVERYONE_RECEIVER = KEYS.add("donation.everyone_receiver", "Everyone").withStyle(ChatFormatting.BLUE);
	public static final TranslationCollector.Fun1 PACKAGE_RECEIVED = KEYS.add1("donation.package_received", "%s received a package!");

	public static final Component REWARDS = KEYS.add("rewards_granted", "You got rewards for playing minigames!").withStyle(ChatFormatting.GOLD);
	public static final TranslationCollector.Fun2 REWARD_ITEM = KEYS.add2("reward_item", " - %sx %s").withStyle(ChatFormatting.GRAY);
	public static final TranslationCollector.Fun1 ELIMINATED = KEYS.add1("eliminated", "\u2620 %s was eliminated!").withStyle(ChatFormatting.GRAY);

	static {
		KEYS.add("starting_in", "Starting in %time%!");

		KEYS.add("donation.antidote_package", "Antidote Package");
		KEYS.add("donation.antidote_package.description", "Help someone avoid bad effects by giving them a Bucket of Milk!");
		KEYS.add("donation.antidote_package.toast", "%sender% sent you an ANTIDOTE PACKAGE!");
		KEYS.add("donation.knockback_package", "Knockback Package");
		KEYS.add("donation.knockback_package.description", "Give someone a Hoe with EXTREME knockback!");
		KEYS.add("donation.knockback_package.toast", "%sender% sent you a KNOCKBACK PACKAGE! Use the Teeter Yeeter™ wisely.");
		KEYS.add("donation.boom_package", "Coconut Bomb Package");
		KEYS.add("donation.boom_package.description", "Give a bunch of throwable, Exploding Coconuts to everyone!");
		KEYS.add("donation.boom_package.toast", "%sender% sent everyone a BOOM PACKAGE of 4 Coconut Bombs!");
		KEYS.add("donation.invisibility_package", "Invisibility Package");
		KEYS.add("donation.invisibility_package.description", "Gives Invisibility to a selected player for 2 minutes");
		KEYS.add("donation.invisibility_package.toast", "%sender% sent you an INVISIBILITY PACKAGE! You are invisible for 2 minutes.");
		KEYS.add("donation.undying_package", "Undying Package");
		KEYS.add("donation.undying_package.description", "Give a select player a Totem of Undying");
		KEYS.add("donation.undying_package.toast", "%sender% sent you an UNDYING PACKAGE with a Totem of Undying!");
		KEYS.add("donation.lightning_crossbow_package", "Lightning Crossbow Package");
		KEYS.add("donation.lightning_crossbow_package.description", "Give a select player a single-use charged Multishot Crossbow that spawns Lightning");
		KEYS.add("donation.lightning_crossbow_package.toast", "%sender% sent you a single-use charged LIGHTNING CROSSBOW!");
		KEYS.add("donation.slowness_package", "Slowness Package");
		KEYS.add("donation.slowness_package.description", "Affect somebody with Slowness V for 2 minutes");
		KEYS.add("donation.slowness_package.toast", "%sender% sent you a SLOWNESS SABOTAGE for 2 minutes!");
		KEYS.add("donation.hunger_package", "Hunger Package");
		KEYS.add("donation.hunger_package.description", "Affect somebody with Hunger III for 30 seconds");
		KEYS.add("donation.hunger_package.toast", "%sender% sent you a HUNGER SABOTAGE for 30 seconds!");
		KEYS.add("donation.random_creeper_package", "Random Creeper Package");
		KEYS.add("donation.random_creeper_package.description", "Spawns a Creeper primed for explosion next to a random player");
		KEYS.add("donation.random_creeper_package.toast", "%sender% sent a RANDOM CREEPER that found you! Look out!");
		KEYS.add("donation.chosen_creeper_package", "Chosen Creeper Package");
		KEYS.add("donation.chosen_creeper_package.description", "A Creeper primed for explosion is spawned right where the selected player is standing");
		KEYS.add("donation.chosen_creeper_package.toast", "%sender% sent you a CREEPER SABOTAGE! Look out!");
		KEYS.add("donation.lightning_strike", "Lightning Strike");
		KEYS.add("donation.lightning_strike.description", "Smite someone with a bolt of lightning!");
		KEYS.add("donation.lightning_strike.toast", "%sender% sent you a LIGHTNING STRIKE! Look out!");
		KEYS.add("donation.lightning_storm", "Lightning Storm Package");
		KEYS.add("donation.lightning_storm.description", "10-20 seconds of lots of lightning strikes all over the map. Just...TONS of lightning.");
		KEYS.add("donation.lightning_storm.toast", "%sender% started a LIGHTNING STORM! Look out!");
		KEYS.add("donation.zombie_invasion_package", "Zombie Invasion Package");
		KEYS.add("donation.zombie_invasion_package.description", "Spawns Zombies all over the map - Walking Dead style!");
		KEYS.add("donation.zombie_invasion_package.toast", "%sender% started a ZOMBIE INVASION! Look out!");
		KEYS.add("donation.meteor_storm_package", "Meteor Storm Package");
		KEYS.add("donation.meteor_storm_package.description", "Send a meteor shower raining down on... everyone");
		KEYS.add("donation.meteor_storm_package.toast", "%sender% started a METEOR STORM! Look out!");
		KEYS.add("donation.meteor_strike_package", "Meteor Strike Package");
		KEYS.add("donation.meteor_strike_package.description", "Strike a random player from the skies with a meteor");
		KEYS.add("donation.meteor_strike_package.toast", "%sender% sent a METEOR STRIKE to a random player! Look out!");
		KEYS.add("donation.forced_player_head_package", "Forced Player Head Package");
		KEYS.add("donation.forced_player_head_package.description", "Force every player in the game to wear the player head of your Minecraft avatar (and Curse of Binding)");
		KEYS.add("donation.forced_player_head_package.toast", "You are now wearing %sender%'s head, and you can't do anything about it!");
		KEYS.add("donation.puffer_package", "Puffer Package");
		KEYS.add("donation.puffer_package.description", "Spawns a Pufferfish in a block of Water where someone is standing");
		KEYS.add("donation.puffer_package.toast", "Where did that Pufferfish come from? %sender% may know.");
		KEYS.add("donation.tapir_party", "Tapir Party");
		KEYS.add("donation.tapir_party.description", "Turn every player into a Tapir for 2 minutes");
		KEYS.add("donation.tapir_party.toast", "Everyone is a Tapir for 2 minutes!");
		KEYS.add("donation.driftwood_dazzle", "Driftwood Dazzle");
		KEYS.add("donation.driftwood_dazzle.description", "Turn every player into Driftwood for 2 minutes");
		KEYS.add("donation.driftwood_dazzle.toast", "Everyone is.. Driftwood? For 2 minutes!");
		KEYS.add("donation.cubera_confusion", "Cubera Confusion");
		KEYS.add("donation.cubera_confusion.description", "Turn every player into fish for 2 minutes");
		KEYS.add("donation.cubera_confusion.toast", "Oops, we're all fish for 2 minutes!");
		KEYS.add("donation.armor_standardization", "Armor Standardization");
		KEYS.add("donation.armor_standardization.description", "Turn all players into Armor Stands for 2 minutes. What?");
		KEYS.add("donation.armor_standardization.toast", "For 2 minutes, you have become an Armor Stand. What?");
		KEYS.add("donation.basilisk_lizard_party", "Basilisk Lizard Party");
		KEYS.add("donation.basilisk_lizard_party.description", "Turn every player into a Basilisk Lizard for 2 minutes");
		KEYS.add("donation.basilisk_lizard_party.toast", "Everyone is a Basilisk Lizard for 2 minutes!");
		KEYS.add("donation.manatee_madness", "Manatee Madness");
		KEYS.add("donation.manatee_madness.description", "Turn every player into a Manatee for 2 minutes");
		KEYS.add("donation.manatee_madness.toast", "Everyone is a Manatee for 2 minutes!");
		KEYS.add("donation.big_leaps", "Many Big Leaps");
		KEYS.add("donation.big_leaps.description", "Turn the dial down on pesky gravity - gravity is reduced for 2 minutes!");
		KEYS.add("donation.big_leaps.toast", "Take a big leap! Gravity has been reduced for 2 minutes!");
		KEYS.add("donation.its_been_fun", "'It's Been Fun'");
		KEYS.add("donation.its_been_fun.description", "It's been fun, but with this package you can end the entire game in style");
		KEYS.add("donation.its_been_fun.toast", "It's been fun, but %sender% has ENDED THE GAME!");
		KEYS.add("donation.player_tornado", "Player Tornado Package");
		KEYS.add("donation.player_tornado.description", "Turn a player into a Tornado. Yes. Literally.");
		KEYS.add("donation.player_tornado.toast", "%sender% turned you into A TORNADO!");
		KEYS.add("donation.player_tornado.title", "You are A TORNADO!");
		KEYS.add("donation.player_tornado_baby", "Tiny Player Tornado Package");
		KEYS.add("donation.player_tornado_baby.description", "Turn a player into a small Tornado. Lasts 33% longer than the big Tornado package.");
		KEYS.add("donation.player_tornado_baby.toast", "%sender% turned you into a BABY TORNADO!");
		KEYS.add("donation.player_tornado_baby.title", "You are a BABY TORNADO!");
		KEYS.add("donation.sharknado", "Sharknado Package");
		KEYS.add("donation.sharknado.description", "What could go wrong? Tornado + Sharks = ???");
		KEYS.add("donation.sharknado.toast", "%sender% has spawned a SHARKNADO!");
		KEYS.add("donation.sharknado.title", "A SHARKNADO has appeared!");
		KEYS.add("donation.surprise_creepers", "Surprise Creepers");
		KEYS.add("donation.surprise_creepers.description", "Spawns 3 Creepers on every plot that explode and absolutely obliterate plants");
		KEYS.add("donation.surprise_creepers.toast", "Surprise! Creepers are attacking your plot!");
		KEYS.add("donation.surprise_creepers.warning", "A wave of surprise creepers is spawning in %time% seconds!");
		KEYS.add("donation.equalize_currency", "Equalize Currency Package");
		KEYS.add("donation.equalize_currency.description", "Redistributes all of the currency in the game by averaging it between everyone.");
		KEYS.add("donation.equalize_currency.toast", "All players' currency has been averaged together!");
		KEYS.add("donation.swap", "Swap Players Package");
		KEYS.add("donation.swap.description", "Swaps every player's position with that of another");
		KEYS.add("donation.swap.toast", "%sender% is swapping everyone with a nearby player!");
		KEYS.add("donation.swap.warning", "You will be swapping with a nearby player in %time% seconds!");
		KEYS.add("donation.helpful_effects", "Helpful Effects Package");
		KEYS.add("donation.helpful_effects.description", "Give a selected player Speed, Regeneration, and Strength for 1 minute");
		KEYS.add("donation.helpful_effects.toast", "%sender% sent you helpful effects for 1 minute!");
		KEYS.add("donation.acid_rain", "Acid Rain");
		KEYS.add("donation.acid_rain.description", "Rain acid down on the players' heads for 1 minute!");
		KEYS.add("donation.acid_rain.toast", "It is raining acid for 1 minute!");
		KEYS.add("donation.speed_boost_1_package", "Speed Boost (1 second)");
		KEYS.add("donation.speed_boost_1_package.description", "Give a chosen player a Speed Boost for 1 second");
		KEYS.add("donation.speed_boost_1_package.toast", "%sender% sent you a SPEED BOOST for 1 second! Wow");
		KEYS.add("donation.speed_boost_5_package", "Speed Boost (5 seconds)");
		KEYS.add("donation.speed_boost_5_package.description", "Give a chosen player a Speed Boost for 5 seconds");
		KEYS.add("donation.speed_boost_5_package.toast", "%sender% sent you a SPEED BOOST for 5 seconds!");
		KEYS.add("donation.speed_boost_30_package", "Speed Boost (30 seconds)");
		KEYS.add("donation.speed_boost_30_package.description", "Give a chosen player a Speed Boost for 30 seconds");
		KEYS.add("donation.speed_boost_30_package.toast", "%sender% sent you a SPEED BOOST for 30 seconds!");
		KEYS.add("donation.slowness_1_package", "Slowness (1 second)");
		KEYS.add("donation.slowness_1_package.description", "Give a chosen player Slowness for 1 second");
		KEYS.add("donation.slowness_1_package.toast", "%sender% sent you a SLOWNESS PACKAGE for 1 second! Wow");
		KEYS.add("donation.slowness_5_package", "Slowness (5 seconds)");
		KEYS.add("donation.slowness_5_package.description", "Give a chosen player Slowness for 5 seconds");
		KEYS.add("donation.slowness_5_package.toast", "%sender% sent you a SLOWNESS PACKAGE for 5 seconds!");
		KEYS.add("donation.slowness_30_package", "Slowness (30 seconds)");
		KEYS.add("donation.slowness_30_package.description", "Give a chosen player Slowness for 30 seconds");
		KEYS.add("donation.slowness_30_package.toast", "%sender% sent you a SLOWNESS PACKAGE for 30 seconds!");

		KEYS.add("event.lightning_storm_event", "Lightning Storm");
		KEYS.add("event.lightning_storm_event.description", "CRACK. That's the sound of the sky as you smite your enemies. Which ones? All of them.");
		KEYS.add("event.lightning_storm_event.toast", "Chat started a LIGHTNING STORM!");
		KEYS.add("event.zombie_invasion_event", "Zombie Invasion");
		KEYS.add("event.zombie_invasion_event.description", "Go Walking Dead on 'em");
		KEYS.add("event.zombie_invasion_event.toast", "Chat started a ZOMBIE INVASION!");
		KEYS.add("event.meteor_shower_event", "Meteor Shower");
		KEYS.add("event.meteor_shower_event.description", "Send a meteor shower raining down on... everyone");
		KEYS.add("event.meteor_shower_event.toast", "Chat started a METEOR SHOWER! Keep an eye on the skies!");
		KEYS.add("event.acid_rain_event", "Acid Rain");
		KEYS.add("event.acid_rain_event.description", "MWAHAHAHA");
		KEYS.add("event.acid_rain_event.toast", "Chat has started ACID RAIN for 1 minute!");
		KEYS.add("event.heatwave_event", "Heat Wave");
		KEYS.add("event.heatwave_event.description", "Slow everyone down with a dastardly heat wave!");
		KEYS.add("event.heatwave_event.toast", "Chat has started a HEATWAVE for 1 minute!");
		KEYS.add("event.hail_event", "Hail Storm");
		KEYS.add("event.hail_event.description", "Hail rains down from the sky! The players better have their Umbrellas ready.");
		KEYS.add("event.hail_event.toast", "Chat has started a HAILSTORM for 1 minute!");

		KEYS.add("levitation.intro1", "Race to be the first to reach the top of the tube!");
		KEYS.add("levitation.intro2", "Use your fishing rod to pull other players down.");

		for (DonationPackageData.PackageType type : DonationPackageData.PackageType.values()) {
			KEYS.add("donation." + type.getSerializedName(), type.getName());
		}
	}
}
