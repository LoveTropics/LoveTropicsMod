package com.lovetropics.minigames.common.minigames.behaviours.instances.statistics;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.statistics.PlayerPlacement;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.mojang.datafixers.Dynamic;

public final class PlaceByStatisticBehavior implements IMinigameBehavior {
	private final StatisticKey<Integer> statistic;
	private final boolean max;

	public PlaceByStatisticBehavior(StatisticKey<Integer> statistic, boolean max) {
		this.statistic = statistic;
		this.max = max;
	}

	@SuppressWarnings("unchecked")
	public static <T> PlaceByStatisticBehavior parse(Dynamic<T> root) {
		StatisticKey<Integer> statistic = (StatisticKey<Integer>) StatisticKey.get(root.get("statistic").asString(""));
		boolean max = root.get("order").asString("max").equalsIgnoreCase("max");
		return new PlaceByStatisticBehavior(statistic, max);
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		PlayerPlacement.Score<Integer> placement;
		if (max) {
			placement = PlayerPlacement.fromMaxScore(minigame, statistic);
		} else {
			placement = PlayerPlacement.fromMinScore(minigame, statistic);
		}

		placement.placeInto(StatisticKey.PLACEMENT);
	}
}
