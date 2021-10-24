package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.drops;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantItemType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

public final class DropPlantItemBehavior implements IGameBehavior {
	public static final Codec<DropPlantItemBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			PlantItemType.CODEC.fieldOf("plant").forGetter(c -> c.plant)
	).apply(instance, DropPlantItemBehavior::new));

	private final PlantItemType plant;

	public DropPlantItemBehavior(PlantItemType plant) {
		this.plant = plant;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(BbPlantEvents.BREAK, (player, plot, plant, pos) -> {
			ItemStack plantItem = game.invoker(BbEvents.CREATE_PLANT_ITEM).createPlantItem(this.plant);
			Block.spawnAsEntity(game.getWorld(), pos, plantItem);
		});
	}
}
