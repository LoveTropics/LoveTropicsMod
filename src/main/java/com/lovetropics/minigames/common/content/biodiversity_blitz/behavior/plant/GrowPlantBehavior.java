package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantType;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.state.PlantState;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public record GrowPlantBehavior(int time, PlantType growInto) implements IGameBehavior {
	public static final MapCodec<GrowPlantBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.INT.fieldOf("time").forGetter(c -> c.time),
			PlantType.CODEC.fieldOf("grow_into").forGetter(c -> c.growInto)
	).apply(i, GrowPlantBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(BbPlantEvents.ADD, (player, plot, plant) -> {
			plant.state().put(GrowTime.KEY, new GrowTime(game.ticks() + this.time));
		});

		events.listen(BbPlantEvents.TICK, (players, plot, plants) -> {
			long ticks = game.ticks();
			if (ticks % 20 != 0) return;

			List<Plant> growPlants = this.collectPlantsToGrow(plants, ticks);
			for (Plant plant : growPlants) {
				this.tryGrowPlant(game, players.iterator().next(), plot, plant);
			}
		});
	}

	private void tryGrowPlant(IGamePhase game, ServerPlayer player, Plot plot, Plant plant) {
		ServerLevel world = game.getWorld();

		PlantSnapshot snapshot = this.removeAndSnapshot(world, plot, plant);

		BlockPos origin = plant.coverage().getOrigin();
		InteractionResultHolder<Plant> result = game.invoker(BbEvents.PLACE_PLANT).placePlant(player, plot, origin, this.growInto);
		if (result.getResult() != InteractionResult.SUCCESS) {
			this.restoreSnapshot(world, plot, snapshot);

			GrowTime growTime = plant.state(GrowTime.KEY);
			if (growTime != null) {
				growTime.next = game.ticks() + this.time;
			}
		}
	}

	private List<Plant> collectPlantsToGrow(List<Plant> plants, long ticks) {
		List<Plant> result = new ArrayList<>();
		for (Plant plant : plants) {
			GrowTime growTime = plant.state(GrowTime.KEY);
			if (growTime != null && ticks >= growTime.next) {
				result.add(plant);
			}
		}

		return result;
	}

	private PlantSnapshot removeAndSnapshot(ServerLevel world, Plot plot, Plant plant) {
		plot.plants.removePlant(plant);

		Long2ObjectMap<BlockState> blocks = new Long2ObjectOpenHashMap<>();
		for (BlockPos pos : plant.coverage()) {
			blocks.put(pos.asLong(), world.getBlockState(pos));
			world.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
		}

		return new PlantSnapshot(plant, blocks);
	}

	private void restoreSnapshot(ServerLevel world, Plot plot, PlantSnapshot snapshot) {
		plot.plants.addPlant(snapshot.plant);

		for (BlockPos pos : snapshot.plant.coverage()) {
			BlockState block = snapshot.blocks.get(pos.asLong());
			if (block != null) {
				world.setBlock(pos, block, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
			}
		}
	}

	static final class GrowTime {
		static final PlantState.Key<GrowTime> KEY = PlantState.Key.create();

		long next;

		GrowTime(long next) {
			this.next = next;
		}
	}

	record PlantSnapshot(Plant plant, Long2ObjectMap<BlockState> blocks) {
	}
}
