package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.PlacementOrder;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerPlacement;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record PlaceByStatisticBehavior(StatisticKey<Integer> statistic, PlacementOrder order) implements IGameBehavior {
	public static final MapCodec<PlaceByStatisticBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StatisticKey.codecFor(Integer.class).fieldOf("statistic").forGetter(c -> c.statistic),
			PlacementOrder.CODEC.optionalFieldOf("order", PlacementOrder.MAX).forGetter(c -> c.order)
	).apply(i, PlaceByStatisticBehavior::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.FINISH, () -> {
			PlayerPlacement.Score<Integer> placement;
			if (order == PlacementOrder.MAX) {
				placement = PlayerPlacement.fromMaxScore(game, statistic);
			} else {
				placement = PlayerPlacement.fromMinScore(game, statistic);
			}

			placement.placeInto(StatisticKey.PLACEMENT);
		});
	}
}
