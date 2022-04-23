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
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

import java.util.stream.IntStream;

public final class BbCurrencyBehavior implements IGameBehavior {
	public static final Codec<BbCurrencyBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Registry.ITEM.fieldOf("item").forGetter(c -> c.item),
				Codec.INT.fieldOf("initial_currency").forGetter(c -> c.initialCurrency),
				DropCalculation.CODEC.fieldOf("drop_calculation").forGetter(c -> c.dropCalculation),
				Codec.LONG.fieldOf("drop_interval").forGetter(c -> c.dropInterval)
		).apply(instance, BbCurrencyBehavior::new);
	});

	private final Item item;
	private final int initialCurrency;

	private final DropCalculation dropCalculation;
	private final long dropInterval;

	private IGamePhase game;
	private CurrencyManager currency;

	public BbCurrencyBehavior(Item item, int initialCurrency, DropCalculation dropCalculation, long dropInterval) {
		this.initialCurrency = initialCurrency;
		this.item = item;
		this.dropCalculation = dropCalculation;
		this.dropInterval = dropInterval;
	}

	@Override
	public void registerState(IGamePhase game, GameStateMap state) {
		this.currency = state.register(CurrencyManager.KEY, new CurrencyManager(game, this.item));
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) throws GameException {
		this.game = game;

		events.listen(BbEvents.ASSIGN_PLOT, (player, plot) -> {
			this.currency.set(player, this.initialCurrency, false);
		});

		events.listen(GamePlayerEvents.THROW_ITEM, (player, item) -> {
			ItemStack stack = item.getItem();
			if (stack.getItem() == this.item) {
				return ActionResultType.FAIL;
			}
			return ActionResultType.PASS;
		});

		events.listen(GamePhaseEvents.TICK, () -> this.currency.tickTracked());

		events.listen(BbEvents.TICK_PLOT, this::tickPlot);
	}

	private void tickPlot(ServerPlayerEntity player, Plot plot) {
		long ticks = this.game.ticks();

		if (ticks % 20 == 0) {
			int nextCurrencyIncrement = this.computeNextCurrency(player, plot);
			if (plot.nextCurrencyIncrement != nextCurrencyIncrement) {
				game.invoker(BbEvents.CURRENCY_INCREMENT_CHANGED).onCurrencyChanged(player, nextCurrencyIncrement, plot.nextCurrencyIncrement);
				plot.nextCurrencyIncrement = nextCurrencyIncrement;
			}
		}

		long intervalTicks = this.dropInterval * 20;
		if (ticks % intervalTicks == 0) {
			this.giveCurrency(player, plot);
		}
	}

	private void giveCurrency(ServerPlayerEntity player, Plot plot) {
		int count = currency.add(player, plot.nextCurrencyIncrement, true);

		player.displayClientMessage(BiodiversityBlitzTexts.currencyAddition(count), true);

		if (count > 0) {
			player.playNotifySound(SoundEvents.ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.24F, 1.0F);
		}
	}

	private int computeNextCurrency(ServerPlayerEntity player, Plot plot) {
		double value = this.computePlotValue(plot);
		return MathHelper.floor(value);
	}

	private double computePlotValue(Plot plot) {
		PlotMetrics metrics = PlotMetrics.compute(plot.plants);

		double value = 1.0;
		for (PlantFamily family : PlantFamily.BIODIVERSITY_VALUES) {
			double familyValue = metrics.valuesByFamily.getDouble(family);
			double diversity = metrics.diversityByFamily.getDouble(family);
			value += dropCalculation.applyFamily(familyValue, diversity);
		}

		return dropCalculation.applyGlobal(value);
	}

	private static final class DropCalculation {
		public static final Codec<DropCalculation> CODEC = RecordCodecBuilder.create(instance -> {
			return instance.group(
					Codec.DOUBLE.fieldOf("base").forGetter(c -> c.base),
					Codec.DOUBLE.fieldOf("bound").forGetter(c -> c.bound),
					Codec.DOUBLE.fieldOf("diversity_factor").forGetter(c -> c.diversityFactor)
			).apply(instance, DropCalculation::new);
		});

		private final double base;
		private final double bound;
		private final double diversityFactor;

		private DropCalculation(double base, double bound, double diversityFactor) {
			this.base = base;
			this.bound = bound;
			this.diversityFactor = diversityFactor;
		}

		public double applyFamily(double familyValue, double diversity) {
			return familyValue * (1.0 + diversityFactor * diversity);
		}

		public double applyGlobal(double value) {
			return bound - bound * Math.pow(base, -value / bound);
		}
	}

	private static final class PlotMetrics {
		private final Reference2DoubleMap<PlantFamily> valuesByFamily;
		private final Reference2DoubleMap<PlantFamily> diversityByFamily;

		private PlotMetrics(Reference2DoubleMap<PlantFamily> valuesByFamily, Reference2DoubleMap<PlantFamily> diversityByFamily) {
			this.valuesByFamily = valuesByFamily;
			this.diversityByFamily = diversityByFamily;
		}

		private static PlotMetrics compute(Iterable<Plant> plants) {
			// Temp fix for plant types not knowing the plant value
			Object2DoubleOpenHashMap<PlantType> valueByType = new Object2DoubleOpenHashMap<>();
			Reference2DoubleOpenHashMap<PlantFamily> values = new Reference2DoubleOpenHashMap<>();

			// Number of plants per plant type in family
			Reference2ObjectMap<PlantFamily, Object2IntOpenHashMap<PlantType>> numberOfPlants = new Reference2ObjectOpenHashMap<>();

			for (Plant plant : plants) {
				// Increment plant type
				numberOfPlants.computeIfAbsent(plant.family(), f -> new Object2IntOpenHashMap<>())
						.addTo(plant.type(), 1);

				// Negative value marks plants that shouldn't count towards biodiversity

				if (plant.value() > 0.0) {
					valueByType.put(plant.type(), plant.value());
				}
			}

			Reference2DoubleMap<PlantFamily> diversity = new Reference2DoubleOpenHashMap<>();

			for (Reference2ObjectMap.Entry<PlantFamily, Object2IntOpenHashMap<PlantType>> entry : Reference2ObjectMaps.fastIterable(numberOfPlants)) {
				// Total amount of plants in this family
				int total = IntStream.of(entry.getValue().values().toIntArray()).sum();
				
				// Amount of plant types
				int types = entry.getValue().size();

				double biodiversity = 0.0;

				// The target is to have an equal amount of plant types in the family
				int target = total / types;

				// Scale the plants, so when you have a lot of a single plant type and less of another, the biodiversity is proportional to that
				for (Object2IntMap.Entry<PlantType> e : Object2IntMaps.fastIterable(entry.getValue())) {
					int plantCount = e.getIntValue();
					double ratio = (double) plantCount / target;
					double percent = (double) plantCount / total;
					biodiversity += Math.max(1, ratio);

					int amountThatShouldCount = plantCount;
					if (total > entry.getKey().getMinBeforeMonoculture()) {
						if (percent > 0.5) {
							// Monoculture detected, only count bottom half
							amountThatShouldCount = plantCount / 2;
						}
					}

					// Multiply the amount of plants by their value to get the total value for this type, add that to the family
					values.addTo(entry.getKey(), amountThatShouldCount * valueByType.getDouble(e.getKey()));
				}

				diversity.put(entry.getKey(), biodiversity);
			}

			return new PlotMetrics(values, diversity);
		}
	}
}
