package com.lovetropics.minigames.common.content.qottott.behavior;

import com.lovetropics.minigames.common.content.qottott.Qottott;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.action.GameActionParameter;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import java.util.function.Supplier;

public record GivePointsAction(StatisticKey<Integer> statistic, int count) implements IGameBehavior {
	public static final MapCodec<GivePointsAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			StatisticKey.INT_CODEC.fieldOf("statistic").forGetter(GivePointsAction::statistic),
			Codec.INT.optionalFieldOf("count", 1).forGetter(GivePointsAction::count)
	).apply(i, GivePointsAction::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			final int count = this.count * context.get(GameActionParameter.COUNT).orElse(1);
			if (count > 0) {
				game.getStatistics().forPlayer(player).incrementInt(statistic, count);
				return true;
			}
			return false;
		});
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return Qottott.GIVE_POINTS;
	}
}
