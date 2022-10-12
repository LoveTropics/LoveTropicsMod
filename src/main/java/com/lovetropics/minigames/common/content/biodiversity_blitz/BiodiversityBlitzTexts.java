package com.lovetropics.minigames.common.content.biodiversity_blitz;

import com.lovetropics.minigames.Constants;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.function.BiConsumer;

public final class BiodiversityBlitzTexts {
	public static void collectTranslations(BiConsumer<String, String> consumer) {
		Keys.collectTranslations(consumer);
	}

	static final class Keys {
		static final String BIODIVERSITY_BLITZ = Constants.MODID + ".minigame.biodiversity_blitz";
		static final String WAVE_WARNING = key("wave_warning");
		static final String DEATH_DECREASE = key("death_decrease");
		static final String DEATH_TITLE = key("death_title");
		static final String CURRENCY_ADDITION = key("currency_addition");
		static final String TRADING = key("trading");
		static final String CAN_ONLY_PLACE_PLANTS = key("can_only_place_plants");
		static final String PLANT_CANNOT_FIT = key("plant_cannot_fit");
		static final String NOT_YOUR_PLOT = key("not_your_plot");

		static void collectTranslations(BiConsumer<String, String> consumer) {
			consumer.accept(BIODIVERSITY_BLITZ, "Biodiversity Blitz");
			consumer.accept(WAVE_WARNING, "A wave is coming soon! Make sure your plant defenses are ready!");
			consumer.accept(TRADING, "Trading");
			consumer.accept(DEATH_DECREASE, "...and lost %s osa points!");
			consumer.accept(DEATH_TITLE, "YOU DIED");
			consumer.accept(CURRENCY_ADDITION, "You gained %s osa points!");
			consumer.accept(CAN_ONLY_PLACE_PLANTS, "You can only place plants you got from the shop!");
			consumer.accept(PLANT_CANNOT_FIT, "This plant cannot fit here!");
			consumer.accept(NOT_YOUR_PLOT, "This is not your plot, you cannot edit here!");
		}

		static String key(String key) {
			return Constants.MODID + ".minigame.biodiversity_blitz." + key;
		}
	}

	public static MutableComponent waveWarning() {
		return new TranslatableComponent(Keys.WAVE_WARNING);
	}

	public static MutableComponent deathDecrease(int count) {
		return new TranslatableComponent(Keys.DEATH_DECREASE, count);
	}

	public static MutableComponent currencyAddition(int amount) {
		Component amountText = new TextComponent(String.valueOf(amount))
				.withStyle(amount > 0 ? ChatFormatting.AQUA : ChatFormatting.RED);
		return new TranslatableComponent(Keys.CURRENCY_ADDITION, amountText);
	}

	public static MutableComponent trading() {
		return new TranslatableComponent(Keys.TRADING);
	}

	public static MutableComponent canOnlyPlacePlants() {
		return new TranslatableComponent(Keys.CAN_ONLY_PLACE_PLANTS);
	}

	public static MutableComponent plantCannotFit() {
		return new TranslatableComponent(Keys.PLANT_CANNOT_FIT);
	}

	public static MutableComponent notYourPlot() {
		return new TranslatableComponent(Keys.NOT_YOUR_PLOT);
	}

	public static MutableComponent deathTitle() {
		return new TranslatableComponent(Keys.DEATH_TITLE);
	}
}
