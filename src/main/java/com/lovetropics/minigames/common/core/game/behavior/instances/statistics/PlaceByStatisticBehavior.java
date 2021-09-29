package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.PlacementOrder;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerPlacement;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class PlaceByStatisticBehavior implements IGameBehavior {
	public static final Codec<PlaceByStatisticBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				StatisticKey.codecFor(Integer.class).fieldOf("statistic").forGetter(c -> c.statistic),
				PlacementOrder.CODEC.optionalFieldOf("order", PlacementOrder.MAX).forGetter(c -> c.order)
		).apply(instance, PlaceByStatisticBehavior::new);
	});

	private final StatisticKey<Integer> statistic;
	private final PlacementOrder order;

	public PlaceByStatisticBehavior(StatisticKey<Integer> statistic, PlacementOrder order) {
		this.statistic = statistic;
		this.order = order;
	}

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
