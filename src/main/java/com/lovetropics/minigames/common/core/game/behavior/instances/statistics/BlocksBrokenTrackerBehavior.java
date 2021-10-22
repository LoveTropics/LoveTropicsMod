package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import net.minecraft.util.ActionResultType;

public final class BlocksBrokenTrackerBehavior implements IGameBehavior {
	public static final Codec<BlocksBrokenTrackerBehavior> CODEC = Codec.unit(BlocksBrokenTrackerBehavior::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.BREAK_BLOCK, (player, pos, state, hand) -> {
			game.getStatistics().forPlayer(player)
					.incrementInt(StatisticKey.BLOCKS_BROKEN, 1);
			return ActionResultType.PASS;
		});
	}
}
