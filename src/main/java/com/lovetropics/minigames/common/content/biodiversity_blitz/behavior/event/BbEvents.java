package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event;

import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantItemType;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantType;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public final class BbEvents {
	public static final GameEventType<AssignPlot> ASSIGN_PLOT = GameEventType.create(AssignPlot.class, listeners -> (player, plot) -> {
		for (AssignPlot listener : listeners) {
			listener.onAssignPlot(player, plot);
		}
	});

	public static final GameEventType<TickPlot> TICK_PLOT = GameEventType.create(TickPlot.class, listeners -> (player, plot) -> {
		for (TickPlot listener : listeners) {
			listener.onTickPlot(player, plot);
		}
	});

	public static final GameEventType<PlacePlant> PLACE_PLANT = GameEventType.create(PlacePlant.class, listeners -> (player, plot, pos, plantType) -> {
		for (PlacePlant listener : listeners) {
			InteractionResultHolder<Plant> result = listener.placePlant(player, plot, pos, plantType);
			if (result.getResult() != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResultHolder.pass(null);
	});

	public static final GameEventType<BreakPlant> BREAK_PLANT = GameEventType.create(BreakPlant.class, listeners -> (player, plot, plant) -> {
		for (BreakPlant listener : listeners) {
			if (listener.breakPlant(player, plot, plant)) {
				return true;
			}
		}
		return false;
	});

	public static final GameEventType<CreatePlantItem> CREATE_PLANT_ITEM = GameEventType.create(CreatePlantItem.class, listeners -> itemType -> {
		for (CreatePlantItem listener : listeners) {
			ItemStack stack = listener.createPlantItem(itemType);
			if (!stack.isEmpty()) {
				return stack;
			}
		}
		return ItemStack.EMPTY;
	});

	public static final GameEventType<PlantsChanged> PLANTS_CHANGED = GameEventType.create(PlantsChanged.class, listeners -> (player, plot) -> {
		for (PlantsChanged listener : listeners) {
			listener.onPlantsChanged(player, plot);
		}
	});

	public static final GameEventType<CurrencyChanged> CURRENCY_CHANGED = GameEventType.create(CurrencyChanged.class, listeners -> (player, value, lastValue) -> {
		for (CurrencyChanged listener : listeners) {
			listener.onCurrencyChanged(player, value, lastValue);
		}
	});

	public static final GameEventType<CurrencyChanged> CURRENCY_ACCUMULATE = GameEventType.create(CurrencyChanged.class, listeners -> (player, value, lastValue) -> {
		for (CurrencyChanged listener : listeners) {
			listener.onCurrencyChanged(player, value, lastValue);
		}
	});

	public static final GameEventType<CurrencyChanged> CURRENCY_INCREMENT_CHANGED = GameEventType.create(CurrencyChanged.class, listeners -> (player, value, lastValue) -> {
		for (CurrencyChanged listener : listeners) {
			listener.onCurrencyChanged(player, value, lastValue);
		}
	});

	public static final GameEventType<GamePlayerEvents.Death> BB_DEATH = GameEventType.create(GamePlayerEvents.Death.class, listeners -> (player, damageSource) -> {
		for (GamePlayerEvents.Death listener : listeners) {
			InteractionResult result = listener.onDeath(player, damageSource);
			if (result != InteractionResult.PASS) {
				return result;
			}
		}
		return InteractionResult.PASS;
	});

	public static final GameEventType<ModifyWaveMobs> MODIFY_WAVE_MODS = GameEventType.create(ModifyWaveMobs.class, listeners -> (entities, random, world, plot, waveIndex) -> {
		for (final ModifyWaveMobs listener : listeners) {
			listener.modifyWave(entities, random, world, plot, waveIndex);
		}
	});

	private BbEvents() {
	}

	public interface AssignPlot {
		void onAssignPlot(ServerPlayer player, Plot plot);
	}

	public interface TickPlot {
		void onTickPlot(Collection<ServerPlayer> player, Plot plot);
	}

	public interface PlacePlant {
		InteractionResultHolder<Plant> placePlant(ServerPlayer player, Plot plot, BlockPos pos, PlantType plantType);
	}

	public interface BreakPlant {
		boolean breakPlant(ServerPlayer player, Plot plot, Plant plant);
	}

	public interface CreatePlantItem {
		ItemStack createPlantItem(PlantItemType itemType);
	}

	public interface PlantsChanged {
		void onPlantsChanged(ServerPlayer player, Plot plot);
	}

	public interface CurrencyChanged {
		void onCurrencyChanged(ServerPlayer player, int value, int lastValue);
	}

	public interface ModifyWaveMobs {
		void modifyWave(Set<Entity> entities, RandomSource random, Level world, Plot plot, int waveIndex);
	}
}
