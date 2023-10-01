package com.lovetropics.minigames.common.content.biodiversity_blitz;

import com.lovetropics.minigames.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.BiConsumer;

public final class BiodiversityBlitzTexts {
	public static void collectTranslations(BiConsumer<String, String> consumer) {
		Keys.collectTranslations(consumer);
	}

	private static final class Keys {
		private static final String BIODIVERSITY_BLITZ = Constants.MODID + ".minigame.biodiversity_blitz";
		private static final String BIODIVERSITY_BLITZ_TEAMS = Constants.MODID + ".minigame.biodiversity_blitz_teams";
		private static final String WAVE_WARNING = key("wave_warning");
		private static final String DEATH_DECREASE = key("death_decrease");
		private static final String DEATH_TITLE = key("death_title");
		private static final String CURRENCY_ADDITION = key("currency_addition");
		private static final String TRADING = key("trading");
		private static final String CAN_ONLY_PLACE_PLANTS = key("can_only_place_plants");
		private static final String PLANT_CANNOT_FIT = key("plant_cannot_fit");
		private static final String PLANT_CANNOT_BE_PLACED_IN_BIOME = key("plant_cannot_be_placed_in_biome");
		private static final String NOT_YOUR_PLOT = key("not_your_plot");

		static void collectTranslations(BiConsumer<String, String> consumer) {
			consumer.accept(BIODIVERSITY_BLITZ, "Biodiversity Blitz");
			consumer.accept(BIODIVERSITY_BLITZ_TEAMS, "Biodiversity Blitz (Teams)");
			consumer.accept(WAVE_WARNING, "A wave is coming soon! Make sure your plant defenses are ready!");
			consumer.accept(TRADING, "Trading");
			consumer.accept(DEATH_DECREASE, "...and lost %s biodiversity points!");
			consumer.accept(DEATH_TITLE, "YOU DIED");
			consumer.accept(CURRENCY_ADDITION, "You gained %s biodiversity points!");
			consumer.accept(CAN_ONLY_PLACE_PLANTS, "You can only place plants you got from the shop!");
			consumer.accept(PLANT_CANNOT_FIT, "This plant cannot fit here!");
			consumer.accept(PLANT_CANNOT_BE_PLACED_IN_BIOME, "This plant cannot be placed in this biome!");
			consumer.accept(NOT_YOUR_PLOT, "This is not your plot, you cannot edit here!");
		}

		static String key(String key) {
			return Constants.MODID + ".minigame.biodiversity_blitz." + key;
		}
	}

	public static MutableComponent waveWarning() {
		return Component.translatable(Keys.WAVE_WARNING);
	}

	public static MutableComponent deathDecrease(int count) {
		return Component.translatable(Keys.DEATH_DECREASE, count);
	}

	public static MutableComponent currencyAddition(int amount) {
		Component amountText = Component.literal(String.valueOf(amount))
				.withStyle(amount > 0 ? ChatFormatting.AQUA : ChatFormatting.RED);
		return Component.translatable(Keys.CURRENCY_ADDITION, amountText);
	}

	public static MutableComponent trading() {
		return Component.translatable(Keys.TRADING);
	}

	public static MutableComponent canOnlyPlacePlants() {
		return Component.translatable(Keys.CAN_ONLY_PLACE_PLANTS);
	}

	public static MutableComponent plantCannotFit() {
		return Component.translatable(Keys.PLANT_CANNOT_FIT);
	}

	public static MutableComponent plantCannotBePlacedInBiome() {
		return Component.translatable(Keys.PLANT_CANNOT_BE_PLACED_IN_BIOME);
	}

	public static MutableComponent notYourPlot() {
		return Component.translatable(Keys.NOT_YOUR_PLOT);
	}

	public static MutableComponent deathTitle() {
		return Component.translatable(Keys.DEATH_TITLE);
	}
}
