package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticsMap;
import com.mojang.serialization.Codec;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public final class KillsTrackerBehavior implements IGameBehavior {
	public static final Codec<KillsTrackerBehavior> CODEC = Codec.unit(KillsTrackerBehavior::new);

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePlayerEvents.DEATH, (player, damageSource) -> {
			GameStatistics statistics = game.getStatistics();
			StatisticsMap playerStatistics = statistics.forPlayer(player);

			Entity source = damageSource.getEntity();
			if (source instanceof ServerPlayer) {
				Player killerPlayer = (Player) source;

				statistics.forPlayer(killerPlayer)
						.withDefault(StatisticKey.KILLS, () -> 0)
						.apply(kills -> kills + 1);

				playerStatistics.set(StatisticKey.KILLED_BY, PlayerKey.from(killerPlayer));
			}

			return InteractionResult.PASS;
		});
	}
}
