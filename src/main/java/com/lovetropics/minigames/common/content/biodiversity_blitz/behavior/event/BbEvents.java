package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event;

import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantCoverage;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantItemType;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantType;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

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

	public static final GameEventType<PlaceAndAddPlant> PLACE_AND_ADD_PLANT = GameEventType.create(PlaceAndAddPlant.class, listeners -> (player, plot, pos, plantType) -> {
		for (PlaceAndAddPlant listener : listeners) {
			ActionResult<Plant> result = listener.placePlant(player, plot, pos, plantType);
			if (result.getType() != ActionResultType.PASS) {
				return result;
			}
		}
		return ActionResult.resultPass(null);
	});

	public static final GameEventType<BreakAndRemovePlant> BREAK_AND_REMOVE_PLANT = GameEventType.create(BreakAndRemovePlant.class, listeners -> (player, plot, plant) -> {
		for (BreakAndRemovePlant listener : listeners) {
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

	private BbEvents() {
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
		void onTickPlants(ServerPlayerEntity player, Plot plot, List<Plant> plants);
	}

	public interface PlaceAndAddPlant {
		ActionResult<Plant> placePlant(ServerPlayerEntity player, Plot plot, BlockPos pos, PlantType plantType);
	}

	public interface PlacePlant {
		@Nullable
		PlantCoverage placePlant(ServerPlayerEntity player, Plot plot, BlockPos pos);
	}

	public interface BreakAndRemovePlant {
		boolean breakPlant(ServerPlayerEntity player, Plot plot, Plant plant);
	}

	public interface BreakPlant {
		void breakPlant(ServerPlayerEntity player, Plot plot, Plant plant, BlockPos pos);
	}

	public interface CreatePlantItem {
		ItemStack createPlantItem(PlantItemType itemType);
	}
}
