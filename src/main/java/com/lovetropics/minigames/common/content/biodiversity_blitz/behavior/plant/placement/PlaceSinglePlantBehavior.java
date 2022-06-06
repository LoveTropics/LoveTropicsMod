package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.placement;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantPlacement;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public record PlaceSinglePlantBehavior(BlockState block) implements IGameBehavior {
	public static final Codec<PlaceSinglePlantBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			MoreCodecs.BLOCK_STATE.fieldOf("block").forGetter(c -> c.block)
	).apply(i, PlaceSinglePlantBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(BbPlantEvents.PLACE, (player, plot, pos) -> new PlantPlacement()
				.covers(pos)
				.places((world, coverage) -> {
					world.setBlockAndUpdate(pos, getPlaceBlock(plot));
					return true;
				}));
	}

	private BlockState getPlaceBlock(Plot plot) {
		BlockState block = this.block;
		if (block.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			block = block.setValue(BlockStateProperties.HORIZONTAL_FACING, plot.forward);
		}
		return block;
	}
}
