package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.GameException;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record SumStatisticBehavior(StatisticKey<Integer> statistic) implements IGameBehavior {
	public static final MapCodec<SumStatisticBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StatisticKey.INT_CODEC.fieldOf("statistic").forGetter(SumStatisticBehavior::statistic)
	).apply(i, SumStatisticBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) throws GameException {
		events.listen(GamePhaseEvents.FINISH, () -> {
			final GameStatistics statistics = game.getStatistics();
			int total = 0;
			for (final PlayerKey player : statistics.getPlayers()) {
				total += statistics.forPlayer(player).getOr(statistic, 0);
			}
			statistics.global().set(statistic, total);
		});
	}
}
