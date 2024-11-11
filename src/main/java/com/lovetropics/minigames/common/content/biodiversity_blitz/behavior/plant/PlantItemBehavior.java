package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitz;
import com.lovetropics.minigames.common.content.biodiversity_blitz.BiodiversityBlitzTexts;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.tutorial.TutorialState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.Plant;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantItemType;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class PlantItemBehavior implements IGameBehavior {
	public static final MapCodec<PlantItemBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			PlantItemType.CODEC.fieldOf("id").forGetter(c -> c.itemType),
			PlantType.CODEC.fieldOf("places").forGetter(c -> c.places),
			MoreCodecs.ITEM_STACK.fieldOf("item").forGetter(c -> c.item)
	).apply(i, PlantItemBehavior::new));

	private final PlantItemType itemType;
	private final PlantType places;
	private final ItemStack item;

	private IGamePhase game;
	private PlotsState plots;
	private TutorialState tutorial;

	public PlantItemBehavior(PlantItemType itemType, PlantType places, ItemStack item) {
		this.itemType = itemType;
		this.places = places;
		this.item = item;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		this.game = game;
		plots = game.state().getOrThrow(PlotsState.KEY);
		tutorial = game.state().getOrThrow(TutorialState.KEY);

		events.listen(GamePlayerEvents.PLACE_BLOCK, this::onPlaceBlock);
		events.listen(BbEvents.CREATE_PLANT_ITEM, this::createPlantDrop);
	}

	private InteractionResult onPlaceBlock(ServerPlayer player, BlockPos pos, BlockState placed, BlockState placedOn, ItemStack placedItemStack) {
		if (!tutorial.isTutorialFinished()) {
			return InteractionResult.PASS;
		}

		ItemStack heldItem = player.getMainHandItem();
		if (!itemType.matches(heldItem)) {
			return InteractionResult.PASS;
		}

		Plot plot = plots.getPlotFor(player);
		if (plot != null && plot.canPlantAt(pos)) {
			if (plot.plants.getPlantAt(pos) != null) {
				return InteractionResult.FAIL;
			}

			// Don't let players place plants inside mob spawns
			if (plot.mobSpawns.contains(pos)) {
				return InteractionResult.FAIL;
			}

			InteractionResultHolder<Plant> result = game.invoker(BbEvents.PLACE_PLANT).placePlant(player, plot, pos, places);
			if (result.getObject() == null) {
				if (result.getResult() == InteractionResult.FAIL) {
					player.displayClientMessage(BiodiversityBlitzTexts.PLANT_CANNOT_FIT.copy().withStyle(ChatFormatting.RED), true);
					player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
				}

				if (result.getResult() == InteractionResult.CONSUME) {
					return InteractionResult.FAIL;
				}
			}

			return result.getResult();
		}

		return InteractionResult.PASS;
	}

	private ItemStack createPlantDrop(PlantItemType itemType) {
		if (this.itemType.equals(itemType)) {
			ItemStack dropItem = item.copy();
			dropItem.set(BiodiversityBlitz.PLANT_COMPONENT, this.itemType);

			return dropItem;
		}

		return ItemStack.EMPTY;
	}
}
