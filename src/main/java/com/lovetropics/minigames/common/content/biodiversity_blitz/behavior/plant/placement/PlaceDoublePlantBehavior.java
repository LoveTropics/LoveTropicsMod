package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.plant.placement;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbPlantEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantCoverage;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.plant.PlantPlacement;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class PlaceDoublePlantBehavior implements IGameBehavior {
	public static final Codec<PlaceDoublePlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MoreCodecs.BLOCK_STATE.fieldOf("block").forGetter(c -> c.block)
	).apply(instance, PlaceDoublePlantBehavior::new));

	private final BlockState block;

	public PlaceDoublePlantBehavior(BlockState block) {
		this.block = block;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(BbPlantEvents.PLACE, (player, plot, pos) -> new PlantPlacement()
				.covers(PlantCoverage.ofDouble(pos))
				.places((world, coverage) -> {
					DoublePlantBlock.placeAt(world, this.block, pos, Block.UPDATE_ALL);
					return true;
				}));
	}
}
