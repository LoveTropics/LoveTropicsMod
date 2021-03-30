package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import net.minecraft.util.ActionResultType;

public final class BlocksBrokenTrackerBehavior implements IGameBehavior {
	public static final Codec<BlocksBrokenTrackerBehavior> CODEC = Codec.unit(BlocksBrokenTrackerBehavior::new);

	@Override
	public void register(IGameInstance registerGame, GameEventListeners events) {
		events.listen(GamePlayerEvents.BREAK_BLOCK, (game, player, pos, state) -> {
			game.getStatistics().forPlayer(player)
					.withDefault(StatisticKey.BLOCKS_BROKEN, () -> 0)
					.apply(count -> count + 1);
			return ActionResultType.PASS;
		});
	}
}
