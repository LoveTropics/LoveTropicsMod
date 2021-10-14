package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.PlotsState;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.*;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class MpPlantBehavior implements IGameBehavior {
	public static final Codec<MpPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlantType.CODEC.fieldOf("id").forGetter(c -> c.plantType),
			PlantPlacement.CODEC.fieldOf("place").forGetter(c -> c.place),
			PlantItemType.CODEC.optionalFieldOf("drops").forGetter(c -> Optional.ofNullable(c.drops)), // TODO: support count
			IGameBehavior.CODEC.listOf().optionalFieldOf("behaviors", Collections.emptyList()).forGetter(c -> c.behaviors)
	).apply(instance, MpPlantBehavior::new));

	private final PlantType plantType;
	private final PlantPlacement place;
	private final PlantItemType drops;
	private final List<IGameBehavior> behaviors;

	private final GameEventListeners plantEvents = new GameEventListeners();

	private IGamePhase game;
	private PlotsState plots;

	public MpPlantBehavior(PlantType plantType, PlantPlacement place, Optional<PlantItemType> drops, List<IGameBehavior> behaviors) {
		this.plantType = plantType;
		this.place = place;
		this.drops = drops.orElse(null);
		this.behaviors = behaviors;
	}

	private static boolean shouldPlantBehaviorHandle(GameEventType<?> type) {
		return type == MpEvents.TICK_PLANTS || type == MpEvents.ADD_PLANT;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		this.plots = game.getState().getOrThrow(PlotsState.KEY);

		events.listen(MpEvents.PLACE_PLANT, this::placePlant);
		events.listen(MpEvents.BREAK_PLANT, this::removePlant);

		events.listen(GamePlayerEvents.BREAK_BLOCK, this::onBreakBlock);

		events.listen(MpEvents.TICK_PLOT, this::onTickPlot);

		EventRegistrar plantEvents = events.redirect(MpPlantBehavior::shouldPlantBehaviorHandle, this.plantEvents);
		for (IGameBehavior behavior : this.behaviors) {
			behavior.register(game, plantEvents);
		}
	}

	private boolean placePlant(ServerPlayerEntity player, Plot plot, BlockPos pos, PlantType plantType) {
		if (!this.plantType.equals(plantType)) {
			return false;
		}

		PlantCoverage coverage = place.place(game.getWorld(), plot, pos);
		if (coverage == null) return false;

		Plant plant = plot.plants.addPlant(plantType, coverage);
		if (plant == null) return false;

		game.invoker(MpEvents.ADD_PLANT).onAddPlant(player, plot, plant);

		return true;
	}

	private boolean removePlant(ServerPlayerEntity player, Plot plot, Plant plant) {
		if (!this.plantType.equals(plant.type())) {
			return false;
		}

		ServerWorld world = game.getWorld();
		for (BlockPos plantPos : plant.coverage()) {
			world.setBlockState(plantPos, Blocks.AIR.getDefaultState(), Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.UPDATE_NEIGHBORS);
		}

		plot.plants.removePlant(plant);

		return true;
	}

	private ActionResultType onBreakBlock(ServerPlayerEntity player, BlockPos pos, BlockState state) {
		Plot plot = plots.getPlotFor(player);
		if (plot == null) {
			return ActionResultType.PASS;
		}

		Plant plant = plot.plants.getPlantAt(pos, this.plantType);
		if (plant != null) {
			return this.onBreakPlant(player, pos, plot, plant);
		} else {
			return ActionResultType.PASS;
		}
	}

	private ActionResultType onBreakPlant(ServerPlayerEntity player, BlockPos pos, Plot plot, Plant plant) {
		ServerWorld world = game.getWorld();

		game.invoker(MpEvents.BREAK_PLANT).breakPlant(player, plot, plant);

		PlantItemType drops = this.drops;
		if (drops != null) {
			ItemStack item = game.invoker(MpEvents.CREATE_PLANT_ITEM).createPlantItem(drops);
			world.addEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item));
		}

		return ActionResultType.FAIL;
	}

	private void onTickPlot(ServerPlayerEntity player, Plot plot) {
		Collection<Plant> plants = plot.plants.getPlantsByType(this.plantType);
		if (!plants.isEmpty()) {
			this.plantEvents.invoker(MpEvents.TICK_PLANTS).onTickPlants(player, plot, plants);
		}
	}
}
