package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerSet;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.statistics.PlacementOrder;
import com.lovetropics.minigames.common.core.game.statistics.PlayerPlacement;
import com.lovetropics.minigames.common.core.game.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.text.StringTextComponent;

public final class DisplayLeaderboardOnFinishBehavior<T extends Comparable<T>> implements IGameBehavior {
	public static final Codec<DisplayLeaderboardOnFinishBehavior<?>> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				StatisticKey.CODEC.fieldOf("statistic").forGetter(c -> c.statistic),
				PlacementOrder.CODEC.optionalFieldOf("order", PlacementOrder.MAX).forGetter(c -> c.order),
				Codec.INT.optionalFieldOf("length", 5).forGetter(c -> c.length)
		).apply(instance, DisplayLeaderboardOnFinishBehavior::createUnchecked);
	});

	private final StatisticKey<T> statistic;
	private final PlacementOrder order;
	private final int length;

	public DisplayLeaderboardOnFinishBehavior(StatisticKey<T> statistic, PlacementOrder order, int length) {
		this.statistic = statistic;
		this.order = order;
		this.length = length;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> DisplayLeaderboardOnFinishBehavior<T> createUnchecked(StatisticKey<?> statistic, PlacementOrder order, int length) {
		return new DisplayLeaderboardOnFinishBehavior<T>((StatisticKey<T>) statistic, order, length);
	}

	@Override
	public void onFinish(IGameInstance minigame) {
		PlayerPlacement.Score<T> placement;
		if (order == PlacementOrder.MAX) {
			placement = PlayerPlacement.fromMaxScore(minigame, statistic);
		} else {
			placement = PlayerPlacement.fromMinScore(minigame, statistic);
		}

		PlayerSet players = minigame.getPlayers();
		players.sendMessage(new StringTextComponent("The game is over! Here are the results:"));
		placement.sendTo(players, length);
	}
}
