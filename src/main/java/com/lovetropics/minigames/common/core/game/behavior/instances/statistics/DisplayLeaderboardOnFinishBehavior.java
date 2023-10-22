package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.content.MinigameTexts;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.player.PlayerSet;
import com.lovetropics.minigames.common.core.game.state.statistics.PlacementOrder;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerPlacement;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

public record DisplayLeaderboardOnFinishBehavior<T extends Comparable<T>>(StatisticKey<T> statistic, PlacementOrder order, int length) implements IGameBehavior {
	public static final MapCodec<DisplayLeaderboardOnFinishBehavior<?>> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StatisticKey.CODEC.fieldOf("statistic").forGetter(c -> c.statistic),
			PlacementOrder.CODEC.optionalFieldOf("order", PlacementOrder.MAX).forGetter(c -> c.order),
			Codec.INT.optionalFieldOf("length", 5).forGetter(c -> c.length)
	).apply(i, DisplayLeaderboardOnFinishBehavior::createUnchecked));

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>> DisplayLeaderboardOnFinishBehavior<T> createUnchecked(StatisticKey<?> statistic, PlacementOrder order, int length) {
		return new DisplayLeaderboardOnFinishBehavior<T>((StatisticKey<T>) statistic, order, length);
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.FINISH, () -> {
			PlayerPlacement.Score<T> placement;
			if (order == PlacementOrder.MAX) {
				placement = PlayerPlacement.fromMaxScore(game, statistic);
			} else {
				placement = PlayerPlacement.fromMinScore(game, statistic);
			}

			PlayerSet players = game.getAllPlayers();
			players.sendMessage(MinigameTexts.RESULTS);
			placement.sendTo(players, length);
		});
	}
}
