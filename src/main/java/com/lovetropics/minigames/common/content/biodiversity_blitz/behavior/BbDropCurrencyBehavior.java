package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.CurrencyManager;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantFamily;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantType;
import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;

import java.util.Collections;
import java.util.Set;

public final class BbDropCurrencyBehavior implements IGameBehavior {
	public static final Codec<BbDropCurrencyBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.fieldOf("interval").forGetter(c -> c.interval)
		).apply(instance, BbDropCurrencyBehavior::new);
	});

	private final long interval;

	private IGamePhase game;

	public BbDropCurrencyBehavior(long interval) {
		this.interval = interval;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		this.game = game;

		events.listen(BbEvents.TICK_PLOT, this::tickPlot);
	}

	private void tickPlot(ServerPlayerEntity player, Plot plot) {
		long ticks = this.game.ticks();

		if (ticks % 20 == 0) {
			plot.nextCurrencyIncrement = this.computeNextCurrency(player, plot);
		}

		long intervalTicks = interval * 20;
		if (ticks % intervalTicks == 0) {
			this.giveCurrency(player, plot);
		}
	}

	private void giveCurrency(ServerPlayerEntity player, Plot plot) {
		int count = CurrencyManager.add(player, plot.nextCurrencyIncrement);

		player.sendStatusMessage(BiodiversityBlitzTexts.currencyAddition(count), true);

		if (count > 0) {
			player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.24F, 1.0F);
		}
	}

	private int computeNextCurrency(ServerPlayerEntity player, Plot plot) {
		boolean atPlot = plot.bounds.contains(player.getPositionVec());
		if (atPlot) {
			double value = this.computePlotValue(plot);
			value = preventCapitalism(value);

			return MathHelper.floor(value);
		} else {
			return 0;
		}
	}

	private double computePlotValue(Plot plot) {
		Reference2DoubleOpenHashMap<PlantFamily> values = new Reference2DoubleOpenHashMap<>();
		Reference2ObjectMap<PlantFamily, Set<PlantType>> counts = new Reference2ObjectOpenHashMap<>();

		for (Plant plant : plot.plants) {
			if (plant.value() < 0) continue; // Negative value marks plants that shouldn't count towards biodiversity

			PlantFamily family = plant.family();
			PlantType type = plant.type();

			values.addTo(family, plant.value());

			Set<PlantType> set = counts.computeIfAbsent(family, f -> new ObjectOpenHashSet<>());
			set.add(type);
		}

		double value = 2.0;
		for (PlantFamily family : PlantFamily.BIODIVERSITY_VALUES) {
			int biodiversity = counts.getOrDefault(family, Collections.emptySet()).size();

			double plantValue = values.getDouble(family) + 0.5;
			plantValue += (biodiversity / 3.0) * plantValue;

			value += plantValue;
		}

		return value;
	}

	private static double preventCapitalism(double count) {
		if (count < 60) {
			return count;
		}

		// \left(60+\frac{x}{20}\right)-1.23^{-\frac{x}{3}}+1
		return 60 + count / 20.0 - Math.pow(1.23, -count / 3.0) + 1;
	}
}
