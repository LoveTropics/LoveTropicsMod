package com.lovetropics.minigames.common.content.qottott.behavior;

import com.lovetropics.lib.codec.MoreCodecs;
import com.lovetropics.minigames.common.content.qottott.Qottott;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticsMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public record LeakyPocketsBehavior(ItemStack item, StatisticKey<Integer> statistic, int interval) implements IGameBehavior {
	public static final MapCodec<LeakyPocketsBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.ITEM_STACK.fieldOf("item").forGetter(LeakyPocketsBehavior::item),
			StatisticKey.INT_CODEC.fieldOf("statistic").forGetter(LeakyPocketsBehavior::statistic),
			Codec.INT.optionalFieldOf("interval", 1).forGetter(LeakyPocketsBehavior::interval)
	).apply(i, LeakyPocketsBehavior::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		final RandomSource random = game.getRandom();
		events.listen(GamePlayerEvents.TICK, player -> {
			if (player.tickCount % interval != 0) {
				return;
			}
			final double chancePerCoin = player.getAttributeValue(Qottott.LEAKY_POCKETS.get());
			if (chancePerCoin <= 0.0) {
				return;
			}
			final StatisticsMap statistics = game.getStatistics().forPlayer(player);
			final int count = statistics.getOr(statistic, 0);
			int dropAmount = sampleDropCount(count, random, chancePerCoin);
			if (dropAmount > 0) {
				statistics.incrementInt(statistic, -dropAmount);
				CoinDropAttributeBehavior.spawnItems(game, player, dropAmount, item);
			}
		});
	}

	private int sampleDropCount(final int count, final RandomSource random, final double chancePerCoin) {
		final double totalChance = chancePerCoin * count;
		int amount = Mth.floor(totalChance);
		if (random.nextFloat() <= totalChance - amount) {
			amount++;
		}
		return Math.min(amount, count);
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return Qottott.LEAKY_POCKETS_BEHAVIOR;
	}
}
