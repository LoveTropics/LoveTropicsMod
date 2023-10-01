package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.google.common.base.Preconditions;
import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.tutorial.TutorialState;
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
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class PlantBehavior implements IGameBehavior {
	public static final Codec<PlantBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			PlantType.CODEC.fieldOf("id").forGetter(c -> c.plantType),
			PlantFamily.CODEC.fieldOf("family").forGetter(c -> c.family),
			Codec.DOUBLE.optionalFieldOf("value", 0.0).forGetter(c -> c.value),
			MoreCodecs.strictOptionalFieldOf(IGameBehavior.CODEC.listOf(), "behaviors", Collections.emptyList()).forGetter(c -> c.behaviors)
	).apply(i, PlantBehavior::new));

	private final PlantType plantType;
	private final PlantFamily family;
	private final double value;
	private final List<IGameBehavior> behaviors;

	private final GameEventListeners plantEvents = new GameEventListeners();

	private IGamePhase game;
	private PlotsState plots;
	private TutorialState tutorial;

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
		this.tutorial = game.getState().getOrThrow(TutorialState.KEY);

		events.listen(BbEvents.PLACE_PLANT, this::placePlant);
		events.listen(BbEvents.BREAK_PLANT, this::breakPlant);

		events.listen(GamePlayerEvents.BREAK_BLOCK, this::onBreakBlock);

		events.listen(BbEvents.TICK_PLOT, this::onTickPlot);

		EventRegistrar plantEvents = events.redirect(PlantBehavior::shouldPlantBehaviorHandle, this.plantEvents);
		for (IGameBehavior behavior : this.behaviors) {
			behavior.register(game, plantEvents);
		}
	}

	private InteractionResultHolder<Plant> placePlant(ServerPlayer player, Plot plot, BlockPos pos, PlantType plantType) {
		if (!this.plantType.equals(plantType)) {
			return InteractionResultHolder.pass(null);
		}

		PlantPlacement placement = plantEvents.invoker(BbPlantEvents.PLACE).placePlant(player, plot, pos);
		if (placement == null) return InteractionResultHolder.pass(null);

		if (placement.getFunctionalCoverage() == null) {
			return InteractionResultHolder.consume(null);
		}

		Plant plant = plot.plants.addPlant(plantType, this.family, this.value, placement);
		if (plant == null) {
			return InteractionResultHolder.fail(null);
		}

		if (placement.place(game.getWorld(), plant.coverage())) {
			plantEvents.invoker(BbPlantEvents.ADD).onAddPlant(player, plot, plant);
			game.invoker(BbEvents.PLANTS_CHANGED).onPlantsChanged(player, plot);

			return InteractionResultHolder.success(plant);
		} else {
			plot.plants.removePlant(plant);
			return InteractionResultHolder.fail(null);
		}
	}

	private boolean breakPlant(ServerPlayer player, Plot plot, Plant plant) {
		if (!this.plantType.equals(plant.type())) {
			return false;
		}

		ServerLevel world = game.getWorld();
		for (BlockPos plantPos : plant.coverage()) {
			FluidState fluidState = world.getFluidState(plantPos);
			world.setBlock(plantPos, fluidState.createLegacyBlock(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
		}

		boolean removed = plot.plants.removePlant(plant);
		if (!removed) {
			return false;
		}

		game.invoker(BbEvents.PLANTS_CHANGED).onPlantsChanged(player, plot);

		return true;
	}

	private InteractionResult onBreakBlock(ServerPlayer player, BlockPos pos, BlockState state, InteractionHand hand) {
		if (!tutorial.isTutorialFinished()) {
			return InteractionResult.FAIL;
		}

		Plot plot = plots.getPlotFor(player);
		if (plot == null) {
			return InteractionResult.PASS;
		}

		if (player.getItemInHand(hand).getItem() instanceof SwordItem) {
			return InteractionResult.FAIL;
		}

		Plant plant = plot.plants.getPlantAt(pos, this.plantType);
		if (plant != null) {
			return this.onBreakPlantBlock(player, pos, plot, plant);
		} else {
			return InteractionResult.PASS;
		}
	}

	private InteractionResult onBreakPlantBlock(ServerPlayer player, BlockPos pos, Plot plot, Plant plant) {
		plantEvents.invoker(BbPlantEvents.BREAK).breakPlant(player, plot, plant, pos);
		game.invoker(BbEvents.BREAK_PLANT).breakPlant(player, plot, plant);

		return InteractionResult.FAIL;
	}

	private void onTickPlot(Collection<ServerPlayer> players, Plot plot) {
		Preconditions.checkArgument(players.size() > 0, "We must always get at least one plot");

		List<Plant> plants = plot.plants.getPlantsByType(this.plantType);
		if (!plants.isEmpty()) {
			this.plantEvents.invoker(BbPlantEvents.TICK).onTickPlants(players, plot, plants);
		}
	}
}
