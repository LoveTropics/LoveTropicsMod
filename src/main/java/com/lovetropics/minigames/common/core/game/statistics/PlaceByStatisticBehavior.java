package com.lovetropics.minigames.common.core.game.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
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
	public void onFinish(IGameInstance minigame) {
		PlayerPlacement.Score<Integer> placement;
		if (order == PlacementOrder.MAX) {
			placement = PlayerPlacement.fromMaxScore(minigame, statistic);
		} else {
			placement = PlayerPlacement.fromMinScore(minigame, statistic);
		}

		placement.placeInto(StatisticKey.PLACEMENT);
	}
}
