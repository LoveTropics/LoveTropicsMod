package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpPlantEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
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
		events.listen(MpPlantEvents.TICK, (player, plot, plants) -> {
			long ticks = game.ticks();
			if (ticks % this.time != 0) return;

			ServerWorld world = game.getWorld();

			List<PlantSnapshot> removedPlants = new ArrayList<>();
			for (Plant plant : plants) {
				removedPlants.add(this.removePlantBlocks(world, plant));
			}

			for (PlantSnapshot snapshot : removedPlants) {
				Plant plant = snapshot.plant;
				plot.plants.removePlant(plant);

				BlockPos origin = plant.coverage().getOrigin();
				ActionResult<Plant> result = game.invoker(MpEvents.PLACE_AND_ADD_PLANT).placePlant(player, plot, origin, this.growInto);
				if (result.getType() != ActionResultType.SUCCESS) {
					this.restoreSnapshot(world, plot, snapshot);
				}
			}
		});
	}

	private PlantSnapshot removePlantBlocks(ServerWorld world, Plant plant) {
		Long2ObjectMap<BlockState> blocks = new Long2ObjectOpenHashMap<>();
		for (BlockPos pos : plant.coverage()) {
			blocks.put(pos.toLong(), world.getBlockState(pos));

			world.setBlockState(pos, Blocks.AIR.getDefaultState(), Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.UPDATE_NEIGHBORS);
		}

		return new PlantSnapshot(plant, blocks);
	}

	private void restoreSnapshot(ServerWorld world, Plot plot, PlantSnapshot snapshot) {
		plot.plants.addPlant(snapshot.plant);

		for (BlockPos pos : snapshot.plant.coverage()) {
			BlockState block = snapshot.blocks.get(pos.toLong());
			if (block != null) {
				world.setBlockState(pos, block, Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.UPDATE_NEIGHBORS);
			}
		}
	}

	static final class PlantSnapshot {
		final Plant plant;
		final Long2ObjectMap<BlockState> blocks;

		PlantSnapshot(Plant plant, Long2ObjectMap<BlockState> blocks) {
			this.plant = plant;
			this.blocks = blocks;
		}
	}
}
