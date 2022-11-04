package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.GameStateMap;
import com.lovetropics.minigames.common.core.game.state.TimedGameState;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommand;
import com.lovetropics.minigames.common.core.game.util.GameBossBar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.lovetropics.minigames.common.util.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.BossEvent;

import java.util.Map;
import java.util.Optional;

public final class TimedGameBehavior implements IGameBehavior {
	public static final Codec<TimedGameBehavior> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.LONG.optionalFieldOf("length", 20L * 60).forGetter(c -> c.length),
			Codec.LONG.optionalFieldOf("close_length", 0L).forGetter(c -> c.closeTime - c.length),
			TemplatedText.CODEC.optionalFieldOf("timer_bar").forGetter(c -> Optional.ofNullable(c.timerBarText)),
			Codec.LONG.optionalFieldOf("countdown_seconds", -1L).forGetter(c -> c.countdownSeconds)
	).apply(i, TimedGameBehavior::new));

	private final long length;
	private final long closeTime;
	private final TemplatedText timerBarText;
	private final long countdownSeconds;

	private GameBossBar timerBar;

	private TimedGameState state;

	public TimedGameBehavior(long length, long closeTime, Optional<TemplatedText> timerBar, long countdownSeconds) {
		this.length = length;
		this.closeTime = closeTime;
		this.timerBarText = timerBar.orElse(null);
		this.countdownSeconds = countdownSeconds;
	}

	@Override
	public void registerState(IGamePhase game, GameStateMap state) {
		this.state = state.register(TimedGameState.KEY, new TimedGameState(length, closeTime));
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.TICK, () -> tick(game));

		if (timerBarText != null) {
			GlobalGameWidgets widgets = GlobalGameWidgets.registerTo(game, events);
			timerBar = widgets.openBossBar(new TextComponent(""), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.NOTCHED_10);
		}

		game.getControlCommands().add("pause", ControlCommand.forInitiator(source -> this.state.setPaused(true)));
		game.getControlCommands().add("unpause", ControlCommand.forInitiator(source -> this.state.setPaused(false)));
	}

	private void tick(IGamePhase game) {
		TimedGameState.TickResult result = this.state.tick(game.ticks());
		switch (result) {
			case RUNNING:
				tickRunning(game);
				break;
			case GAME_OVER:
				game.invoker(GameLogicEvents.GAME_OVER).onGameOver();
				break;
			case CLOSE:
				game.requestStop(GameStopReason.finished());
				break;
		}
	}

	private void tickRunning(IGamePhase game) {
		long ticksRemaining = state.getTicksRemaining();
		if (ticksRemaining % 20 == 0) {
			long secondsRemaining = ticksRemaining / 20;

			if (secondsRemaining <= countdownSeconds) {
				playCountdownSound(game, secondsRemaining);
			}

			if (timerBar != null) {
				timerBar.setTitle(this.getTimeRemainingText(game, ticksRemaining));
				timerBar.setProgress((float) ticksRemaining / length);
			}
		}
	}

	private void playCountdownSound(IGamePhase game, long seconds) {
		float pitch = seconds == 0 ? 1.5F : 1.0F;
		game.getAllPlayers().playSound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.MASTER, 0.8F, pitch);
	}

	private Component getTimeRemainingText(IGamePhase game, long ticksRemaining) {
		long secondsRemaining = ticksRemaining / SharedConstants.TICKS_PER_SECOND;

		Component timeText = new TextComponent(Util.formatMinutesSeconds(secondsRemaining)).withStyle(ChatFormatting.AQUA);
		Component gameNameText = game.getDefinition().getName().copy().withStyle(ChatFormatting.AQUA);

		return timerBarText.apply(Map.of("time", timeText, "game", gameNameText));
	}
}
