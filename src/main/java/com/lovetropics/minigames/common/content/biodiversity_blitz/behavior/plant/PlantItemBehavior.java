package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantItemType;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;

public final class PlantItemBehavior implements IGameBehavior {
	public static final Codec<PlantItemBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlantItemType.CODEC.fieldOf("id").forGetter(c -> c.itemType),
			PlantType.CODEC.fieldOf("places").forGetter(c -> c.places),
			MoreCodecs.ITEM_STACK.fieldOf("item").forGetter(c -> c.item)
	).apply(instance, PlantItemBehavior::new));

	private final PlantItemType itemType;
	private final PlantType places;
	private final ItemStack item;

	private IGamePhase game;
	private PlotsState plots;

	public PlantItemBehavior(PlantItemType itemType, PlantType places, ItemStack item) {
		this.itemType = itemType;
		this.places = places;
		this.item = item;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		this.plots = game.getState().getOrThrow(PlotsState.KEY);

		events.listen(GamePlayerEvents.PLACE_BLOCK, this::onPlaceBlock);
		events.listen(BbEvents.CREATE_PLANT_ITEM, this::createPlantDrop);
	}

	private ActionResultType onPlaceBlock(ServerPlayerEntity player, BlockPos pos, BlockState placed, BlockState placedOn) {
		ItemStack heldItem = player.getHeldItemMainhand();
		if (!this.itemType.matches(heldItem)) {
			return ActionResultType.PASS;
		}

		Plot plot = plots.getPlotFor(player);
		if (plot != null && plot.plantBounds.contains(pos)) {
			if (plot.plants.getPlantAt(pos) != null) {
				return ActionResultType.FAIL;
			}

			ActionResult<Plant> result = game.invoker(BbEvents.PLACE_AND_ADD_PLANT).placePlant(player, plot, pos, this.places);
			if (result.getResult() == null) {
				// TODO: indicate failure
			}

			return result.getType();
		}

		return ActionResultType.PASS;
	}

	private ItemStack createPlantDrop(PlantItemType itemType) {
		if (this.itemType.equals(itemType)) {
			ItemStack dropItem = this.item.copy();
			this.itemType.applyTo(dropItem);

			return dropItem;
		}

		return ItemStack.EMPTY;
	}
}
