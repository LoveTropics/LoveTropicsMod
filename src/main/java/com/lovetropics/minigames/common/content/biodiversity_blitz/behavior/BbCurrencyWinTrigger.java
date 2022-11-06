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
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public final class BbCurrencyWinTrigger implements IGameBehavior {
	public static final Codec<BbCurrencyWinTrigger> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.fieldOf("threshold_currency").forGetter(c -> c.thresholdCurrency)
	).apply(i, BbCurrencyWinTrigger::new));

	private final int thresholdCurrency;

	private final List<ServerPlayer> winnerCandidates = new ArrayList<>();

	private boolean gameOver;

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
			List<ServerPlayer> players = this.winnerCandidates;
			if (!gameOver && !players.isEmpty()) {
				ServerPlayer player = this.selectWinningPlayer(plots, currency, players);
				if (player != null) {
					// Teleport all players to winning player's plot
					for (ServerPlayer otherPlayer : game.getAllPlayers()) {
						if (otherPlayer != player) {
							otherPlayer.teleportTo(player.getX(), player.getY() + 0.5, player.getZ());
						}
					}

					this.triggerWin(game, player);
				}
			}
		});
	}

	@Nullable
	private ServerPlayer selectWinningPlayer(PlotsState plots, CurrencyManager currency, Collection<ServerPlayer> players) {
		Comparator<ServerPlayer> comparator = Comparator.comparingInt(currency::get)
				.thenComparingInt(player -> {
					Plot plot = plots.getPlotFor(player);
					return plot != null ? plot.nextCurrencyIncrement : 0;
				});

		return players.stream().max(comparator).orElse(null);
	}

	private void triggerWin(IGamePhase game, ServerPlayer player) {
		game.getStatistics().global().set(StatisticKey.WINNING_PLAYER, PlayerKey.from(player));

		game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(player.getDisplayName());
		game.invoker(GameLogicEvents.GAME_OVER).onGameOver();

		gameOver = true;
	}
}
