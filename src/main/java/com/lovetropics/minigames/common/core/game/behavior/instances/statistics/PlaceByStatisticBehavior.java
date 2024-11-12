package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.GameStatistics;
import com.lovetropics.minigames.common.core.game.state.statistics.PlacementOrder;
import com.lovetropics.minigames.common.core.game.state.statistics.Placement;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Comparator;
import java.util.function.Supplier;

public record PlaceByStatisticBehavior(StatisticKey<Integer> statistic, PlacementOrder order) implements IGameBehavior {
	public static final MapCodec<PlaceByStatisticBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StatisticKey.INT_CODEC.fieldOf("statistic").forGetter(c -> c.statistic),
			PlacementOrder.CODEC.optionalFieldOf("order", PlacementOrder.MAX).forGetter(c -> c.order)
	).apply(i, PlaceByStatisticBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.FINISH, () -> {
			Placement.fromPlayerScore(order, game, statistic).placeInto(StatisticKey.PLACEMENT);

			final GameStatistics statistics = game.statistics();
			statistics.getPlayers().stream()
					.min(Comparator.comparing(player -> statistics.forPlayer(player).getOr(StatisticKey.PLACEMENT, Integer.MAX_VALUE)))
					.ifPresent(winningPlayer -> statistics.global().set(StatisticKey.WINNING_PLAYER, winningPlayer));
		});
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return GameBehaviorTypes.PLACE_BY_STATISTIC;
	}
}
