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
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public record LeakyPocketsAction(ItemStack item, StatisticKey<Integer> statistic, IntProvider count, float chance) implements IGameBehavior {
	public static final MapCodec<LeakyPocketsAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			MoreCodecs.ITEM_STACK.fieldOf("item").forGetter(LeakyPocketsAction::item),
			StatisticKey.INT_CODEC.fieldOf("statistic").forGetter(LeakyPocketsAction::statistic),
			IntProvider.POSITIVE_CODEC.fieldOf("count").forGetter(LeakyPocketsAction::count),
			Codec.floatRange(0.0f, 1.0f).fieldOf("chance").forGetter(LeakyPocketsAction::chance)
	).apply(i, LeakyPocketsAction::new));

	@Override
	public void register(final IGamePhase game, final EventRegistrar events) {
		final RandomSource random = game.getRandom();
		events.listen(GameActionEvents.APPLY_TO_PLAYER, (context, player) -> {
			if (random.nextFloat() > chance) {
				return false;
			}
			final StatisticsMap statistics = game.getStatistics().forPlayer(player);
			final int amount = Math.min(statistics.getOr(statistic, 0), count.sample(random));
			if (amount > 0) {
				statistics.incrementInt(statistic, -amount);
				CoinDropAttributeBehavior.spawnItems(game, player, amount, item);
			}
			return true;
		});
	}

	@Override
	public Supplier<? extends GameBehaviorType<?>> behaviorType() {
		return Qottott.LEAKY_POCKETS;
	}
}
