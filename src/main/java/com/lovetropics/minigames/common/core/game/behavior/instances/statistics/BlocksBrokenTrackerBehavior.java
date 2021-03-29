package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.BlockEvent;

public final class BlocksBrokenTrackerBehavior implements IGameBehavior {
	public static final Codec<BlocksBrokenTrackerBehavior> CODEC = Codec.unit(BlocksBrokenTrackerBehavior::new);

	@Override
	public void onPlayerBreakBlock(IGameInstance minigame, ServerPlayerEntity player, BlockPos pos, BlockState state, BlockEvent.BreakEvent event) {
		minigame.getStatistics().forPlayer(player)
				.withDefault(StatisticKey.BLOCKS_BROKEN, () -> 0)
				.apply(count -> count + 1);
	}
}
