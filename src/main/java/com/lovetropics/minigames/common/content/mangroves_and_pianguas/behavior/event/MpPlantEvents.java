package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantCoverage;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.List;

public final class MpPlantEvents {
	public static final GameEventType<Add> ADD = GameEventType.create(Add.class, listeners -> (player, plot, plant) -> {
		for (Add listener : listeners) {
			listener.onAddPlant(player, plot, plant);
		}
	});

	public static final GameEventType<Tick> TICK = GameEventType.create(Tick.class, listeners -> (player, plot, plants) -> {
		for (Tick listener : listeners) {
			listener.onTickPlants(player, plot, plants);
		}
	});

	public static final GameEventType<Place> PLACE = GameEventType.create(Place.class, listeners -> (player, plot, pos) -> {
		for (Place listener : listeners) {
			PlantCoverage coverage = listener.placePlant(player, plot, pos);
			if (coverage != null) {
				return coverage;
			}
		}
		return null;
	});

	public static final GameEventType<Break> BREAK = GameEventType.create(Break.class, listeners -> (player, plot, plant, pos) -> {
		for (Break listener : listeners) {
			listener.breakPlant(player, plot, plant, pos);
		}
	});

	private MpPlantEvents() {
	}

	public interface Add {
		void onAddPlant(ServerPlayerEntity player, Plot plot, Plant plant);
	}

	public interface Tick {
		void onTickPlants(ServerPlayerEntity player, Plot plot, List<Plant> plants);
	}

	public interface Place {
		@Nullable
		PlantCoverage placePlant(ServerPlayerEntity player, Plot plot, BlockPos pos);
	}

	public interface Break {
		void breakPlant(ServerPlayerEntity player, Plot plot, Plant plant, BlockPos pos);
	}
}
