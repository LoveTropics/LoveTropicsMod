package com.lovetropics.minigames.common.content.biodiversity_blitz;

import com.lovetropics.minigames.Constants;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.BiConsumer;

public final class BiodiversityBlitzTexts {
	public static void collectTranslations(BiConsumer<String, String> consumer) {
		Keys.collectTranslations(consumer);
	}

	static final class Keys {
		static final String BIODIVERSITY_BLITZ = Constants.MODID + ".minigame.biodiversity_blitz";
		static final String WAVE_WARNING = key("wave_warning");
		static final String DEATH_DECREASE = key("death_decrease");
		static final String CURRENCY_ADDITION = key("currency_addition");
		static final String TRADING = key("trading");
		static final String CAN_ONLY_PLACE_PLANTS = key("can_only_place_plants");
		static final String PLANT_CANNOT_FIT = key("plant_cannot_fit");
		static final String NOT_YOUR_PLOT = key("not_your_plot");

		static void collectTranslations(BiConsumer<String, String> consumer) {
			consumer.accept(BIODIVERSITY_BLITZ, "Biodiversity Blitz");
			consumer.accept(WAVE_WARNING, "A wave is coming soon!");
			consumer.accept(TRADING, "Trading");
			consumer.accept(DEATH_DECREASE, "You died and lost %s currency!");
			consumer.accept(CURRENCY_ADDITION, "You gained %s currency!");
			consumer.accept(CAN_ONLY_PLACE_PLANTS, "You can only place plants you got from the shop!");
			consumer.accept(PLANT_CANNOT_FIT, "This plant cannot fit here!");
			consumer.accept(NOT_YOUR_PLOT, "This is not your plot, you cannot edit here!");
		}

		static String key(String key) {
			return Constants.MODID + ".minigame.biodiversity_blitz." + key;
		}
	}

	public static IFormattableTextComponent waveWarning() {
		return new TranslationTextComponent(Keys.WAVE_WARNING);
	}

	public static IFormattableTextComponent deathDecrease(int count) {
		return new TranslationTextComponent(Keys.DEATH_DECREASE, count);
	}

	public static IFormattableTextComponent currencyAddition(int amount) {
		return new TranslationTextComponent(Keys.CURRENCY_ADDITION, amount);
	}

	public static IFormattableTextComponent trading() {
		return new TranslationTextComponent(Keys.TRADING);
	}

	public static IFormattableTextComponent canOnlyPlacePlants() {
		return new TranslationTextComponent(Keys.CAN_ONLY_PLACE_PLANTS);
	}

	public static IFormattableTextComponent plantCannotFit() {
		return new TranslationTextComponent(Keys.PLANT_CANNOT_FIT);
	}

	public static IFormattableTextComponent notYourPlot() {
		return new TranslationTextComponent(Keys.NOT_YOUR_PLOT);
	}
}
