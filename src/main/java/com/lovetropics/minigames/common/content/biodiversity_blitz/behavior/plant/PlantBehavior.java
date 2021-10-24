package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantFamily;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantPlacement;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventType;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.SwordItem;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.Constants;

import java.util.Collections;
import java.util.List;

public final class PlantBehavior implements IGameBehavior {
	public static final Codec<PlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlantType.CODEC.fieldOf("id").forGetter(c -> c.plantType),
			MoreCodecs.stringVariants(PlantFamily.values(), PlantFamily::friendlyName).fieldOf("family").forGetter(c -> c.family),
			Codec.DOUBLE.optionalFieldOf("value", 0.0).forGetter(c -> c.value),
			IGameBehavior.CODEC.listOf().optionalFieldOf("behaviors", Collections.emptyList()).forGetter(c -> c.behaviors)
	).apply(instance, PlantBehavior::new));

	private final PlantType plantType;
	private final PlantFamily family;
	private final double value;
	private final List<IGameBehavior> behaviors;

	private final GameEventListeners plantEvents = new GameEventListeners();

	private IGamePhase game;
	private PlotsState plots;

	public PlantBehavior(PlantType plantType, PlantFamily family, double value, List<IGameBehavior> behaviors) {
		this.plantType = plantType;
		this.behaviors = behaviors;
		this.family = family;
		this.value = value;
	}

	private static boolean shouldPlantBehaviorHandle(GameEventType<?> type) {
		return type == BbPlantEvents.ADD || type == BbPlantEvents.TICK
				|| type == BbPlantEvents.PLACE || type == BbPlantEvents.BREAK;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		this.plots = game.getState().getOrThrow(PlotsState.KEY);

		events.listen(BbEvents.PLACE_AND_ADD_PLANT, this::addAndPlantPlant);
		events.listen(BbEvents.BREAK_AND_REMOVE_PLANT, this::breakAndRemovePlant);

		events.listen(GamePlayerEvents.BREAK_BLOCK, this::onBreakBlock);

		events.listen(BbEvents.TICK_PLOT, this::onTickPlot);

		EventRegistrar plantEvents = events.redirect(PlantBehavior::shouldPlantBehaviorHandle, this.plantEvents);
		for (IGameBehavior behavior : this.behaviors) {
			behavior.register(game, plantEvents);
		}
	}

	private ActionResult<Plant> addAndPlantPlant(ServerPlayerEntity player, Plot plot, BlockPos pos, PlantType plantType) {
		if (!this.plantType.equals(plantType)) {
			return ActionResult.resultPass(null);
		}

		PlantPlacement placement = plantEvents.invoker(BbPlantEvents.PLACE).placePlant(player, plot, pos);
		if (placement == null) return ActionResult.resultPass(null);

		Plant plant = plot.plants.addPlant(plantType, this.family, this.value, placement);
		if (plant == null) {
			player.sendStatusMessage(BiodiversityBlitzTexts.plantCannotFit().mergeStyle(TextFormatting.RED), true);
			player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);

			return ActionResult.resultFail(null);
		}

		if (placement.place(game.getWorld())) {
			plantEvents.invoker(BbPlantEvents.ADD).onAddPlant(player, plot, plant);
			return ActionResult.resultSuccess(plant);
		} else {
			plot.plants.removePlant(plant);
			return ActionResult.resultFail(null);
		}
	}

	private boolean breakAndRemovePlant(ServerPlayerEntity player, Plot plot, Plant plant) {
		if (!this.plantType.equals(plant.type())) {
			return false;
		}

		ServerWorld world = game.getWorld();
		for (BlockPos plantPos : plant.coverage()) {
			FluidState fluidState = world.getFluidState(plantPos);
			world.setBlockState(plantPos, fluidState.getBlockState(), Constants.BlockFlags.BLOCK_UPDATE | Constants.BlockFlags.UPDATE_NEIGHBORS);
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
		plantEvents.invoker(BbPlantEvents.BREAK).breakPlant(player, plot, plant, pos);
		game.invoker(BbEvents.BREAK_AND_REMOVE_PLANT).breakPlant(player, plot, plant);

		return ActionResultType.FAIL;
	}

	private void onTickPlot(ServerPlayerEntity player, Plot plot) {
		List<Plant> plants = plot.plants.getPlantsByType(this.plantType);
		if (!plants.isEmpty()) {
			this.plantEvents.invoker(BbPlantEvents.TICK).onTickPlants(player, plot, plants);
		}
	}
}
