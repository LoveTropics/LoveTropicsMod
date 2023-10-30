package com.lovetropics.minigames.common.content.biodiversity_blitz;

import com.lovetropics.minigames.Constants;
import com.lovetropics.minigames.common.content.biodiversity_blitz.entity.BbMobSpawner;
import com.lovetropics.minigames.common.core.game.util.TranslationCollector;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.BiConsumer;

public final class BiodiversityBlitzTexts {
	private static final TranslationCollector KEYS = new TranslationCollector(Constants.MODID + ".minigame.biodiversity_blitz.");

	public static final Component WAVE_WARNING = KEYS.add("wave_warning", "A wave is coming soon! Make sure your plant defenses are ready!");
	public static final Component WAVE_INCOMING = KEYS.add("wave_incoming", "Wave Incoming!");
	public static final TranslationCollector.Fun1 WAVE_NUMBER = KEYS.add1("wave_number", "Wave %s");
	public static final Component TRADING = KEYS.add("trading", "Trading");
	private static final TranslationCollector.Fun1 DEATH_DECREASE = KEYS.add1("death_decrease", "...and lost %s biodiversity points!");
	public static final Component DEATH_TITLE = KEYS.add("death_title", "YOU DIED");
	private static final TranslationCollector.Fun1 CURRENCY_ADDITION = KEYS.add1("currency_addition", "You gained %s biodiversity points!");
	public static final Component CAN_ONLY_PLACE_PLANTS = KEYS.add("can_only_place_plants", "You can only place plants you got from the shop!");
	public static final Component PLANT_CANNOT_FIT = KEYS.add("plant_cannot_fit", "This plant cannot fit here!");
	public static final Component PLANT_CANNOT_BE_PLACED_IN_BIOME = KEYS.add("plant_cannot_be_placed_in_biome", "This plant cannot be placed in this biome!");
	public static final Component NOT_YOUR_PLOT = KEYS.add("not_your_plot", "This is not your plot, you cannot edit here!");
	public static final Component SHIFT_FOR_MORE_INFORMATION = KEYS.add("shift_for_more_information", "Hold Shift for more information");
	public static final Component SIDEBAR_TITLE = KEYS.add("sidebar.title", "Biodiversity Blitz");
	private static final TranslationCollector.Fun1 SIDEBAR_PLAYER_HEADER = KEYS.add1("sidebar.player_header", "Player: %s");
	private static final Component SIDEBAR_PLAYER_HEADER_POINTS = KEYS.add("sidebar.player_header.points", "Points (+per drop)");
	private static final TranslationCollector.Fun2 SIDEBAR_PLAYER = KEYS.add2("sidebar.player", "%s: %s");
	private static final TranslationCollector.Fun2 SIDEBAR_PLAYER_POINTS = KEYS.add2("sidebar.player.points", "%s (+ %s)");
	public static final Component SIDEBAR_AND_MORE = KEYS.add("sidebar.and_more", "... and more!");
	private static final TranslationCollector.Fun2 SEND_MOBS_TOOLTIP = KEYS.add2("send_mobs.tooltip", "%sx %s");

	public static final Component SHOP = KEYS.add("shop", "Shop");
	public static final Component PLANT_SHOP = KEYS.add("plant_shop", "Plant Shop");
	public static final Component MOB_SHOP = KEYS.add("mob_shop", "Mob Shop");

	public static final Component WAVE_SPAWNED = KEYS.add("wave_spawned", "A wave has spawned! Your plant defense may not yet be self-sufficient, use your sword to defend your plot!");
	public static final Component[] TIPS = {
			KEYS.add("tips.diversity", "Make sure to have a plot with many different plant types to gain more Biodiversity Points!"),
			KEYS.add("tips.diffuse", "The Commelina Diffusa can put out the Crocosmia's fires. Be careful of where you plant them!"),
			KEYS.add("tips.pianguas", "Mangrove trees produce pianguas in mud that can be harvested via right click to sell at the store!"),
			KEYS.add("tips.creeper", "Beware the creeper. The legends say that its explosion can level even the sturdiest of trees..."),
			KEYS.add("tips.wave_difficulty", "The waves will get harder as time goes on, so be quick!"),
			KEYS.add("tips.visit", "You can visit other players' plots to learn from how they've set up their plot!"),
			KEYS.add("tips.pumpkins", "Mobs loooove pumpkins. Place them in strategic places to create elaborate traps!"),
			KEYS.add("tips.monoculture", "Be careful of having too much of the same plant, monocultures can hurt your point production!"),
			KEYS.add("tips.crops", "Crops are weaker than other plants, so put them in places where mobs can't get to them!"),
			KEYS.add("tips.oak", "Rumours say that if you're patient enough, oak trees will drop golden apples. Could it possibly be true?"),
			KEYS.add("tips.tilling", "If you change your mind about tilling a piece of land, simply right click it with your hoe to make it into grass again!")
	};

	static {
		KEYS.add("intro1", "Welcome to Biodiversity Blitz!");
		KEYS.add("intro2", "As the professional conservationist that you surely are, you must turn this bare plot into a thriving ecosystem.");
		KEYS.add("intro3", "\nYou can buy plants and items from the shop using your 'Biodiversity points' and place them in your plot!");
		KEYS.add("intro4", "\nThese plants may provide food, defense, or even attack the invasive species. Every plant you place improves the number of biodiversity points received per turn.");
		KEYS.add("intro5", "\nInvasive species will be spawning soon! But if you construct an ecosystem that is resilient enough, you may be able to survive.");
		KEYS.add("intro6", "\nMake sure to have a large variety of different plants in your plot- diversity in an ecosystem only makes you stronger!");
		KEYS.add("intro7", "\nYou win the game once you accumulate the target amount of Biodiversity Points - you can check your progress in the top-left of your screen!");
		KEYS.add("intro8", "\nGood luck!");

		KEYS.add("teams.intro1", "Welcome to Biodiversity Blitz!");
		KEYS.add("teams.intro2", "As the professional conservationist that you surely are, you must turn this bare plot into a thriving ecosystem.");
		KEYS.add("teams.intro3", "\nYou can buy plants and items from the shop using your 'Biodiversity points' and place them in your plot!");
		KEYS.add("teams.intro4", "\nThese plants may provide food, defense, or even attack the invasive species. Every plant you place improves the number of biodiversity points received per turn.");
		KEYS.add("teams.intro5", "\nInvasive species will be spawning soon! But if you construct an ecosystem that is resilient enough, you may be able to survive.");
		KEYS.add("teams.intro6", "\nMake sure to have a large variety of different plants in your plot- diversity in an ecosystem only makes you stronger!");
		KEYS.add("teams.intro7", "\nYou win the game once your team accumulates the target amount of Biodiversity Points - you can check your progress in the top-left of your screen!");
		KEYS.add("teams.intro8", "\nGood luck!");
	}

	public static void collectTranslations(BiConsumer<String, String> consumer) {
		KEYS.forEach(consumer);

		consumer.accept(Constants.MODID + ".minigame.biodiversity_blitz", "Biodiversity Blitz");
		consumer.accept(Constants.MODID + ".minigame.biodiversity_blitz_teams", "Biodiversity Blitz (Teams)");
		consumer.accept(BbMobSpawner.BbEntityTypes.CREEPER.getTranslationKey(), "Creeper");
		consumer.accept(BbMobSpawner.BbEntityTypes.HUSK.getTranslationKey(), "Husk");
		consumer.accept(BbMobSpawner.BbEntityTypes.PILLAGER.getTranslationKey(), "Pillager");
	}

	public static MutableComponent deathDecrease(int count) {
		return DEATH_DECREASE.apply(count);
	}

	public static MutableComponent currencyAddition(int amount) {
		Component amountText = Component.literal(String.valueOf(amount))
				.withStyle(amount > 0 ? ChatFormatting.AQUA : ChatFormatting.RED);
		return CURRENCY_ADDITION.apply(amountText);
	}

	public static MutableComponent sidebarPlayerHeader() {
		return SIDEBAR_PLAYER_HEADER.apply(
				SIDEBAR_PLAYER_HEADER_POINTS.copy().withStyle(ChatFormatting.GOLD)
		).withStyle(ChatFormatting.AQUA);
	}

	public static MutableComponent sidebarPlayer(Component name, int points, int increment) {
		return SIDEBAR_PLAYER.apply(
				name,
				SIDEBAR_PLAYER_POINTS.apply(points, increment).withStyle(ChatFormatting.GOLD)
		).withStyle(ChatFormatting.AQUA);
	}

	public static MutableComponent sendMobsTooltip(BbMobSpawner.BbEntityTypes entity, int count) {
		return SEND_MOBS_TOOLTIP.apply(count, entity.getName().withStyle(ChatFormatting.GOLD));
	}
}
