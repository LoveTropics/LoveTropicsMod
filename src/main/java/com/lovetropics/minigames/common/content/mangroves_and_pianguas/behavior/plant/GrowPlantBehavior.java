package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantCoverage;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.ArrayList;
import java.util.List;

public final class GrowPlantBehavior implements IGameBehavior {
	public static final Codec<GrowPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("time").forGetter(c -> c.time),
			PlantType.CODEC.fieldOf("grow_into").forGetter(c -> c.growInto)
	).apply(instance, GrowPlantBehavior::new));

	private final int time;
	private final PlantType growInto;

	public GrowPlantBehavior(int time, PlantType growInto) {
		this.time = time;
		this.growInto = growInto;
	}

	// TODO: rather store the planted time than going on an interval
	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(MpEvents.TICK_PLANTS, (player, plot, plants) -> {
			long ticks = game.ticks();
			if (ticks % this.time != 0) return;

			ServerWorld world = game.getWorld();

			List<Plant> removedPlants = new ArrayList<>();
			for (Plant plant : plants) {
				for (BlockPos pos : plant.coverage()) {
					world.setBlockState(pos, Blocks.AIR.getDefaultState(), Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.UPDATE_NEIGHBORS);
				}

				removedPlants.add(plant);
			}

			for (Plant plant : removedPlants) {
				game.invoker(MpEvents.BREAK_PLANT).breakPlant(player, plot, plant);
				plot.plants.removePlant(plant);

				PlantCoverage coverage = plant.coverage();

				BlockPos origin = coverage.getOrigin();
				game.invoker(MpEvents.PLACE_PLANT).placePlant(player, plot, origin, this.growInto);
			}
		});
	}
}
