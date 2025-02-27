package com.lovetropics.minigames.common.core.game.behavior.instances.action;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorTypes;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.util.LinearSpline;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

import java.util.function.Supplier;

public record SetStatisticAction(
		StatisticKey<Integer> statistic,
		int value,
		LinearSpline valueByPlayerCount
) implements IGameBehavior {
	public static final MapCodec<SetStatisticAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StatisticKey.typedCodec(Integer.class).fieldOf("statistic").forGetter(SetStatisticAction::statistic),
			Codec.INT.optionalFieldOf("value", 0).forGetter(SetStatisticAction::value),
			LinearSpline.CODEC.optionalFieldOf("by_player_count", LinearSpline.constant(0.0f)).forGetter(SetStatisticAction::valueByPlayerCount)
	).apply(i, SetStatisticAction::new));

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GameActionEvents.APPLY, context -> {
			game.statistics().global().set(statistic, resolve(game));
			return true;
		});
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, target) -> {
			game.statistics().forPlayer(target).set(statistic, resolve(game));
			return true;
		});
	}

	private int resolve(IGamePhase game) {
		int valueByPlayerCount = Mth.floor(this.valueByPlayerCount.get(game.participants().size()));
		return Math.max(value, valueByPlayerCount);
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return GameBehaviorTypes.SET_STATISTIC;
	}
}
