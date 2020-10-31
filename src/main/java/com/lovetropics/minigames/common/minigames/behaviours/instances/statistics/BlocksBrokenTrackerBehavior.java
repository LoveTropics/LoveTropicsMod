package com.lovetropics.minigames.common.minigames.behaviours.instances.statistics;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.world.BlockEvent;

public final class BlocksBrokenTrackerBehavior implements IMinigameBehavior {
	public static <T> BlocksBrokenTrackerBehavior parse(Dynamic<T> root) {
		return new BlocksBrokenTrackerBehavior();
	}

	@Override
	public void onPlayerBreakBlock(IMinigameInstance minigame, ServerPlayerEntity player, BlockPos pos, BlockState state, BlockEvent.BreakEvent event) {
		minigame.getStatistics().forPlayer(player)
				.withDefault(StatisticKey.BLOCKS_BROKEN, () -> 0)
				.apply(count -> count + 1);
	}
}
