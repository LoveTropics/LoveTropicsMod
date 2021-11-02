package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.CurrencyTargetState;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.CurrencyManager;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.Plot;
import com.lovetropics.minigames.common.content.biodiversity_blitz.plot.PlotsState;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class BbCurrencyWinTrigger implements IGameBehavior {
	public static final Codec<BbCurrencyWinTrigger> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.fieldOf("threshold_currency").forGetter(c -> c.thresholdCurrency)
		).apply(instance, BbCurrencyWinTrigger::new);
	});

	private final int thresholdCurrency;

	private final List<ServerPlayerEntity> winnerCandidates = new ArrayList<>();

	public BbCurrencyWinTrigger(int thresholdCurrency) {
		this.thresholdCurrency = thresholdCurrency;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		PlotsState plots = game.getState().getOrThrow(PlotsState.KEY);
		CurrencyManager currency = game.getState().getOrThrow(CurrencyManager.KEY);

		CurrencyTargetState target = new CurrencyTargetState(this.thresholdCurrency);
		GameClientState.applyGlobally(target, events);

		events.listen(BbEvents.CURRENCY_ACCUMULATE, (player, value, lastValue) -> {
			if (value >= this.thresholdCurrency) {
				this.winnerCandidates.add(player);
			}
		});

		events.listen(GamePhaseEvents.TICK, () -> {
			List<ServerPlayerEntity> players = this.winnerCandidates;
			if (!players.isEmpty()) {
				ServerPlayerEntity player = this.selectWinningPlayer(plots, currency, players);
				if (player != null) {
					this.triggerWin(game, player);
				}
			}
		});
	}

	@Nullable
	private ServerPlayerEntity selectWinningPlayer(PlotsState plots, CurrencyManager currency, Collection<ServerPlayerEntity> players) {
		Comparator<ServerPlayerEntity> comparator = Comparator.comparingInt(currency::get)
				.thenComparingInt(player -> {
					Plot plot = plots.getPlotFor(player);
					return plot != null ? plot.nextCurrencyIncrement : 0;
				});

		return players.stream().max(comparator).orElse(null);
	}

	private void triggerWin(IGamePhase game, ServerPlayerEntity player) {
		game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(player.getDisplayName());
		game.invoker(GameLogicEvents.GAME_OVER).onGameOver();

		game.getStatistics().global().set(StatisticKey.WINNING_PLAYER, PlayerKey.from(player));
	}
}
