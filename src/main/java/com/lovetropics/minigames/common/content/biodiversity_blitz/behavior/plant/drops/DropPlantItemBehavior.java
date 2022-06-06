package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.drops;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantItemType;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;

public record DropPlantItemBehavior(PlantItemType plant) implements IGameBehavior {
	public static final Codec<DropPlantItemBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			PlantItemType.CODEC.fieldOf("plant").forGetter(c -> c.plant)
	).apply(i, DropPlantItemBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(BbPlantEvents.BREAK, (player, plot, plant, pos) -> {
			ItemStack plantItem = game.invoker(BbEvents.CREATE_PLANT_ITEM).createPlantItem(this.plant);
			Block.popResource(game.getWorld(), pos, plantItem);
		});
	}
}
