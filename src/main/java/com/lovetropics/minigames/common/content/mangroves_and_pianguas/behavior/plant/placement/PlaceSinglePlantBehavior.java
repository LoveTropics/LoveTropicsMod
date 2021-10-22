package com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.plant.placement;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.behavior.event.MpPlantEvents;
import com.lovetropics.minigames.common.content.mangroves_and_pianguas.plot.plant.PlantCoverage;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.state.properties.BlockStateProperties;

public final class PlaceSinglePlantBehavior implements IGameBehavior {
	public static final Codec<PlaceSinglePlantBehavior> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			MoreCodecs.BLOCK_STATE.fieldOf("block").forGetter(c -> c.block)
	).apply(instance, PlaceSinglePlantBehavior::new));

	private final BlockState block;

	public PlaceSinglePlantBehavior(BlockState block) {
		this.block = block;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(MpPlantEvents.PLACE, (player, plot, pos) -> {
			game.getWorld().setBlockState(pos, getPlaceBlock(player));
			return PlantCoverage.of(pos);
		});
	}

	private BlockState getPlaceBlock(ServerPlayerEntity player) {
		BlockState block = this.block;
		if (block.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			block = block.with(BlockStateProperties.HORIZONTAL_FACING, player.getHorizontalFacing().getOpposite());
		}
		return block;
	}
}
