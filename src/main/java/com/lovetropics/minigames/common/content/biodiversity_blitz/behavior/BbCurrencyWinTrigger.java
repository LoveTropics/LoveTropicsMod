package com.lovetropics.minigames.common.content.biodiversity_blitz.behavior;

import com.lovetropics.minigames.common.content.biodiversity_blitz.behavior.event.BbEvents;
import com.lovetropics.minigames.common.content.biodiversity_blitz.client_state.CurrencyTargetState;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.client_state.GameClientState;
import com.lovetropics.minigames.common.core.game.state.statistics.PlayerKey;
import com.lovetropics.minigames.common.core.game.state.statistics.StatisticKey;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;

public final class BbCurrencyWinTrigger implements IGameBehavior {
	public static final Codec<BbCurrencyWinTrigger> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.fieldOf("threshold_currency").forGetter(c -> c.thresholdCurrency)
		).apply(instance, BbCurrencyWinTrigger::new);
	});

	private final int thresholdCurrency;

	public BbCurrencyWinTrigger(int thresholdCurrency) {
		this.thresholdCurrency = thresholdCurrency;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		CurrencyTargetState target = new CurrencyTargetState(this.thresholdCurrency);
		GameClientState.applyGlobally(target, events);

		events.listen(BbEvents.CURRENCY_CHANGED, (player, value, lastValue) -> {
			if (value >= this.thresholdCurrency) {
				this.triggerWin(game, player);
			}
		});
	}

	private void triggerWin(IGamePhase game, ServerPlayerEntity player) {
		game.invoker(GameLogicEvents.WIN_TRIGGERED).onWinTriggered(player.getDisplayName());
		game.invoker(GameLogicEvents.GAME_OVER).onGameOver();

		game.getStatistics().global().set(StatisticKey.WINNING_PLAYER, PlayerKey.from(player));
	}
}
