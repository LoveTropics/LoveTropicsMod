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
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;

import java.util.Collections;
import java.util.Set;

public final class BbCurrencyBehavior implements IGameBehavior {
	public static final Codec<BbCurrencyBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Registry.ITEM.fieldOf("item").forGetter(c -> c.item),
				Codec.INT.fieldOf("initial_currency").forGetter(c -> c.initialCurrency),
				Codec.LONG.fieldOf("drop_interval").forGetter(c -> c.dropInterval)
		).apply(instance, BbCurrencyBehavior::new);
	});

	private final Item item;
	private final int initialCurrency;
	private final long dropInterval;

	private IGamePhase game;
	private CurrencyManager currency;

	public BbCurrencyBehavior(Item item, int initialCurrency, long dropInterval) {
		this.initialCurrency = initialCurrency;
		this.item = item;
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
			this.currency.set(player, this.initialCurrency);
		});

		events.listen(GamePlayerEvents.THROW_ITEM, (player, item) -> {
			ItemStack stack = item.getItem();
			if (stack.getItem() == this.item) {
				player.inventory.addItemStackToInventory(stack);
				player.sendContainerToPlayer(player.openContainer);
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
		int count = currency.add(player, plot.nextCurrencyIncrement);

		player.sendStatusMessage(BiodiversityBlitzTexts.currencyAddition(count), true);

		if (count > 0) {
			player.playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.PLAYERS, 0.24F, 1.0F);
		}
	}

	private int computeNextCurrency(ServerPlayerEntity player, Plot plot) {
		boolean atPlot = plot.walls.getBounds().contains(player.getPositionVec());
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
