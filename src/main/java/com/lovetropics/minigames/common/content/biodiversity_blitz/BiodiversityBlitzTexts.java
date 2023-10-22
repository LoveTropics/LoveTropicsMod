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
	public static final Component TRADING = KEYS.add("trading", "Trading");
	private static final TranslationCollector.Fun1 DEATH_DECREASE = KEYS.add1("death_decrease", "...and lost %s biodiversity points!");
	public static final Component DEATH_TITLE = KEYS.add("death_title", "YOU DIED");
	private static final TranslationCollector.Fun1 CURRENCY_ADDITION = KEYS.add1("currency_addition", "You gained %s biodiversity points!");
	public static final Component CAN_ONLY_PLACE_PLANTS = KEYS.add("can_only_place_plants", "You can only place plants you got from the shop!");
	public static final Component PLANT_CANNOT_FIT = KEYS.add("plant_cannot_fit", "This plant cannot fit here!");
	public static final Component PLANT_CANNOT_BE_PLACED_IN_BIOME = KEYS.add("plant_cannot_be_placed_in_biome", "This plant cannot be placed in this biome!");
	public static final Component NOT_YOUR_PLOT = KEYS.add("not_your_plot", "This is not your plot, you cannot edit here!");

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
}
