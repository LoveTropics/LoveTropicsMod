package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantItemType;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantType;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public final class MpEvents {
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

	public static final GameEventType<AddPlant> ADD_PLANT = GameEventType.create(AddPlant.class, listeners -> (player, plot, plant) -> {
		for (AddPlant listener : listeners) {
			listener.onAddPlant(player, plot, plant);
		}
	});

	public static final GameEventType<TickPlants> TICK_PLANTS = GameEventType.create(TickPlants.class, listeners -> (player, plot, plants) -> {
		for (TickPlants listener : listeners) {
			listener.onTickPlants(player, plot, plants);
		}
	});

	public static final GameEventType<PlacePlant> PLACE_PLANT = GameEventType.create(PlacePlant.class, listeners -> (player, plot, pos, plantType) -> {
		for (PlacePlant listener : listeners) {
			if (listener.placePlant(player, plot, pos, plantType)) {
				return true;
			}
		}
		return false;
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

	private MpEvents() {
	}

	public interface AssignPlot {
		void onAssignPlot(ServerPlayerEntity player, Plot plot);
	}

	public interface TickPlot {
		void onTickPlot(ServerPlayerEntity player, Plot plot);
	}

	public interface AddPlant {
		void onAddPlant(ServerPlayerEntity player, Plot plot, Plant plant);
	}

	public interface TickPlants {
		void onTickPlants(ServerPlayerEntity player, Plot plot, Iterable<Plant> plants);
	}

	public interface PlacePlant {
		boolean placePlant(ServerPlayerEntity player, Plot plot, BlockPos pos, PlantType plantType);
	}

	public interface BreakPlant {
		boolean breakPlant(ServerPlayerEntity player, Plot plot, Plant plant);
	}

	public interface CreatePlantItem {
		ItemStack createPlantItem(PlantItemType itemType);
	}
}
