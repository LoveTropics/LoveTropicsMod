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

		events.listen(BbEvents.ASSIGN_PLOT, this::onPlotChanged);
		events.listen(BbEvents.PLANTS_CHANGED, this::onPlotChanged);
	}

	private void tickPlot(ServerPlayerEntity player, Plot plot) {
		long ticks = this.game.ticks();
		long intervalTicks = interval * 20;
		if (ticks % intervalTicks != 0) {
			return;
		}

		int count = CurrencyManager.add(player, plot.nextCurrencyIncrement);

		player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.24F, 1.0F);
		player.sendStatusMessage(BiodiversityBlitzTexts.currencyAddition(count), true);
	}

	private void onPlotChanged(ServerPlayerEntity player, Plot plot) {
		plot.nextCurrencyIncrement = computeNextCurrency(plot);
	}

	private int computeNextCurrency(Plot plot) {
		Reference2DoubleOpenHashMap<PlantFamily> values = new Reference2DoubleOpenHashMap<>();
		Reference2ObjectMap<PlantFamily, Set<PlantType>> counts = new Reference2ObjectOpenHashMap<>();

		for (Plant plant : plot.plants) {
			PlantFamily family = plant.family();
			PlantType type = plant.type();

			values.addTo(family, plant.value());

			// TODO: plants like saplings should not contribute to biodiversity!
			Set<PlantType> set = counts.computeIfAbsent(family, f -> new ObjectOpenHashSet<>());
			set.add(type);
		}

		double count = 2.0;
		for (PlantFamily family : PlantFamily.VALUES) {
			int biodiversity = counts.getOrDefault(family, Collections.emptySet()).size();

			double value = values.getDouble(family) + 0.5;
			value += (biodiversity / 3.0) * value;

			count += value;
		}

		count = preventCapitalism(count);

		return MathHelper.floor(count);
	}

	private static double preventCapitalism(double count) {
		if (count < 60) {
			return count;
		}

		// \left(60+\frac{x}{20}\right)-1.23^{-\frac{x}{3}}+1
		return 60 + count / 20.0 - Math.pow(1.23, -count / 3.0) + 1;
	}
}
