package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant;

import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpPlantEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.Plot;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.PlotsState;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.Plant;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantCoverage;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantType;
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
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public final class MpPlantBehavior implements IGameBehavior {
	public static final Codec<MpPlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlantType.CODEC.fieldOf("id").forGetter(c -> c.plantType),
			IGameBehavior.CODEC.listOf().optionalFieldOf("behaviors", Collections.emptyList()).forGetter(c -> c.behaviors)
	).apply(instance, MpPlantBehavior::new));

	private final PlantType plantType;
	private final List<IGameBehavior> behaviors;

	private final GameEventListeners plantEvents = new GameEventListeners();

	private IGamePhase game;
	private PlotsState plots;

	public MpPlantBehavior(PlantType plantType, List<IGameBehavior> behaviors) {
		this.plantType = plantType;
		this.behaviors = behaviors;
	}

	private static boolean shouldPlantBehaviorHandle(GameEventType<?> type) {
		return type == MpPlantEvents.ADD || type == MpPlantEvents.TICK
				|| type == MpPlantEvents.PLACE || type == MpPlantEvents.BREAK;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		this.plots = game.getState().getOrThrow(PlotsState.KEY);

		events.listen(MpEvents.PLACE_AND_ADD_PLANT, this::placePlant);
		events.listen(MpEvents.BREAK_AND_REMOVE_PLANT, this::removePlant);

		events.listen(GamePlayerEvents.BREAK_BLOCK, this::onBreakBlock);

		events.listen(MpEvents.TICK_PLOT, this::onTickPlot);

		EventRegistrar plantEvents = events.redirect(MpPlantBehavior::shouldPlantBehaviorHandle, this.plantEvents);
		for (IGameBehavior behavior : this.behaviors) {
			behavior.register(game, plantEvents);
		}
	}

	@Nullable
	private Plant placePlant(ServerPlayerEntity player, Plot plot, BlockPos pos, PlantType plantType) {
		if (!this.plantType.equals(plantType)) {
			return null;
		}

		PlantCoverage coverage = plantEvents.invoker(MpPlantEvents.PLACE).placePlant(player, plot, pos);
		if (coverage == null) return null;

		Plant plant = plot.plants.addPlant(plantType, coverage);
		plantEvents.invoker(MpPlantEvents.ADD).onAddPlant(player, plot, plant);

		return plant;
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

	private ActionResultType onBreakBlock(ServerPlayerEntity player, BlockPos pos, BlockState state, Hand hand) {
		Plot plot = plots.getPlotFor(player);
		if (plot == null) {
			return ActionResultType.PASS;
		}

		if (player.getHeldItem(hand).getItem() instanceof SwordItem) {
			return ActionResultType.FAIL;
		}

		Plant plant = plot.plants.getPlantAt(pos, this.plantType);
		if (plant != null) {
			return this.onBreakPlant(player, pos, plot, plant);
		} else {
			return ActionResultType.PASS;
		}
	}

	private ActionResultType onBreakPlant(ServerPlayerEntity player, BlockPos pos, Plot plot, Plant plant) {
		plantEvents.invoker(MpPlantEvents.BREAK).breakPlant(player, plot, plant, pos);
		game.invoker(MpEvents.BREAK_AND_REMOVE_PLANT).breakPlant(player, plot, plant);

		return ActionResultType.FAIL;
	}

	private void onTickPlot(ServerPlayerEntity player, Plot plot) {
		List<Plant> plants = plot.plants.getPlantsByType(this.plantType);
		if (!plants.isEmpty()) {
			this.plantEvents.invoker(MpPlantEvents.TICK).onTickPlants(player, plot, plants);
		}
	}
}
