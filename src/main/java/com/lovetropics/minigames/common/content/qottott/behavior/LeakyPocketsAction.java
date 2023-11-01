package com.lovetropics.minigames.common.content.qottott.behavior;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.qottott.Qottott;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameActionEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticsMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public record LeakyPocketsAction(ItemStack item, StatisticKey<Integer> statistic, float chancePerCoin, int interval) implements IGameBehavior {
	public static final MapCodec<LeakyPocketsAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.ITEM_STACK.fieldOf("item").forGetter(LeakyPocketsAction::item),
			StatisticKey.INT_CODEC.fieldOf("statistic").forGetter(LeakyPocketsAction::statistic),
			Codec.floatRange(0.0f, 1.0f).fieldOf("chance_per_coin").forGetter(LeakyPocketsAction::chancePerCoin),
			Codec.INT.optionalFieldOf("interval", 1).forGetter(LeakyPocketsAction::interval)
	).apply(i, LeakyPocketsAction::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		final RandomSource random = game.getRandom();
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			if (player.tickCount % interval != 0) {
				return false;
			}
			final StatisticsMap statistics = game.getStatistics().forPlayer(player);
			final int count = statistics.getOr(statistic, 0);
			int dropAmount = sampleDropCount(count, random);
			if (dropAmount >= 0) {
				statistics.incrementInt(statistic, -dropAmount);
				CoinDropAttributeBehavior.spawnItems(game, player, dropAmount, item);
				return true;
			}
			return false;
		});
	}

	private int sampleDropCount(int count, RandomSource random) {
		final float totalChance = chancePerCoin * count;
		int amount = Mth.floor(totalChance);
		if (random.nextFloat() <= totalChance - amount) {
			amount++;
		}
		return Math.min(amount, count);
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return Qottott.LEAKY_POCKETS;
	}
}
