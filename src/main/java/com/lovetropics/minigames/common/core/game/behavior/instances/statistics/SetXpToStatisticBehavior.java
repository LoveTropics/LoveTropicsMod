package com.lovetropics.minigames.common.core.game.behavior.instances.statistics;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticsMap;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.function.Supplier;

public record SetXpToStatisticBehavior(StatisticKey<Integer> statistic) implements IGameBehavior {
	public static final MapCodec<SetXpToStatisticBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StatisticKey.INT_CODEC.fieldOf("statistic").forGetter(SetXpToStatisticBehavior::statistic)
	).apply(i, SetXpToStatisticBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		events.listen(GamePlayerEvents.TICK, player -> {
			final StatisticsMap statistics = game.statistics().forPlayer(player);
			final int level = statistics.getInt(statistic);
			if (level != player.experienceLevel) {
				player.setExperienceLevels(level);
				player.experienceProgress = 1.0f;
			}
		});
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return GameBehaviorTypes.SET_XP_TO_STATISTIC;
	}
}
