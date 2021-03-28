package com.lovetropics.minigames.common.minigames.behaviours.instances.statistics;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.statistics.PlayerPlacement;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class PlaceByStatisticBehavior implements IMinigameBehavior {
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
	public void onFinish(IMinigameInstance minigame) {
		PlayerPlacement.Score<Integer> placement;
		if (order == PlacementOrder.MAX) {
			placement = PlayerPlacement.fromMaxScore(minigame, statistic);
		} else {
			placement = PlayerPlacement.fromMinScore(minigame, statistic);
		}

		placement.placeInto(StatisticKey.PLACEMENT);
	}
}
