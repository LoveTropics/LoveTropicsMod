package com.lovetropics.minigames.common.minigames.behaviours.instances.statistics;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.PlayerSet;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.lovetropics.minigames.common.minigames.statistics.PlayerPlacement;
import com.lovetropics.minigames.common.minigames.statistics.StatisticKey;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.text.StringTextComponent;

public final class DisplayLeaderboardOnFinishBehavior<T extends Comparable<T>> implements IMinigameBehavior {
	private final StatisticKey<T> statistic;
	private final boolean max;
	private final int length;

	public DisplayLeaderboardOnFinishBehavior(StatisticKey<T> statistic, boolean max, int length) {
		this.statistic = statistic;
		this.max = max;
		this.length = length;
	}

	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>, D> DisplayLeaderboardOnFinishBehavior<T> parse(Dynamic<D> root) {
		StatisticKey<T> statistic = (StatisticKey<T>) StatisticKey.get(root.get("statistic").asString(""));
		boolean max = root.get("order").asString("max").equalsIgnoreCase("max");
		int length = root.get("length").asInt(5);
		return new DisplayLeaderboardOnFinishBehavior<>(statistic, max, length);
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		PlayerPlacement.Score<T> placement;
		if (max) {
			placement = PlayerPlacement.fromMaxScore(minigame, statistic);
		} else {
			placement = PlayerPlacement.fromMinScore(minigame, statistic);
		}

		PlayerSet players = minigame.getPlayers();
		players.sendMessage(new StringTextComponent("The game is over! Here are the results:"));
		placement.sendTo(players, length);
	}
}
