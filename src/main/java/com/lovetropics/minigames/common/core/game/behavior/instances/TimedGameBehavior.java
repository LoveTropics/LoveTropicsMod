package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameStopReason;
import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.state.control.ControlCommand;
import com.lovetropics.minigames.common.core.game.util.GameBossBar;
import com.lovetropics.minigames.common.core.game.util.GlobalGameWidgets;
import com.lovetropics.minigames.common.core.game.util.TemplatedText;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.BossInfo;

import java.util.Optional;

public final class TimedGameBehavior implements IGameBehavior {
	public static final Codec<TimedGameBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.optionalFieldOf("length", 20L * 60).forGetter(c -> c.length),
				Codec.LONG.optionalFieldOf("close_length", 0L).forGetter(c -> c.closeTime - c.length),
				TemplatedText.CODEC.optionalFieldOf("timer_bar").forGetter(c -> Optional.ofNullable(c.timerBarText)),
				Codec.LONG.optionalFieldOf("countdown_seconds", -1L).forGetter(c -> c.countdownSeconds)
		).apply(instance, TimedGameBehavior::new);
	});

	private final long length;
	private final long closeTime;
	private final TemplatedText timerBarText;
	private final long countdownSeconds;

	private GameBossBar timerBar;

	private long timeRemaining;
	private long closeAtTime;
	private boolean paused;

	public TimedGameBehavior(long length, long closeTime, Optional<TemplatedText> timerBar, long countdownSeconds) {
		this.length = length;
		this.closeTime = closeTime;
		this.timerBarText = timerBar.orElse(null);
		this.countdownSeconds = countdownSeconds;
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		timeRemaining = length;

		events.listen(GamePhaseEvents.TICK, () -> tick(game));

		if (timerBarText != null) {
			GlobalGameWidgets widgets = GlobalGameWidgets.registerTo(game, events);
			timerBar = widgets.openBossBar(new StringTextComponent(""), BossInfo.Color.GREEN, BossInfo.Overlay.PROGRESS);
		}

		game.getControlCommands().add("pause", ControlCommand.forInitiator(source -> paused = true));
		game.getControlCommands().add("unpause", ControlCommand.forInitiator(source -> paused = false));
	}

	private void tick(IGamePhase game) {
		long ticks = game.ticks();
		if (closeAtTime != 0 && ticks >= closeTime) {
			game.requestStop(GameStopReason.finished());
			return;
		}

		if (!paused && timeRemaining > 0) {
			tickRunning(game, ticks);
		}
	}

	private void tickRunning(IGamePhase game, long ticks) {
		long ticksRemaining = --timeRemaining;
		if (ticksRemaining == 0) {
			game.invoker(GameLogicEvents.GAME_OVER).onGameOver();
			closeAtTime = ticks + closeTime;
			return;
		}

		if (ticksRemaining % 20 == 0 && timerBar != null) {
			long secondsRemaining = ticksRemaining / 20;

			if (secondsRemaining <= countdownSeconds) {
				playCountdownSound(game, secondsRemaining);
			}

			timerBar.setTitle(this.getTimeRemainingText(ticksRemaining));
			timerBar.setProgress((float) ticksRemaining / length);
		}
	}

	private void playCountdownSound(IGamePhase game, long seconds) {
		float pitch = seconds == 0 ? 1.5F : 1.0F;
		game.getAllPlayers().playSound(SoundEvents.ENTITY_ARROW_HIT_PLAYER, SoundCategory.MASTER, 0.8F, pitch);
	}

	private ITextComponent getTimeRemainingText(long ticksRemaining) {
		long secondsRemaining = ticksRemaining / 20;

		long minutes = secondsRemaining / 60;
		long seconds = secondsRemaining % 60;
		String time = String.format("%02d:%02d", minutes, seconds);

		return timerBarText.apply(new StringTextComponent(time).mergeStyle(TextFormatting.GRAY));
	}
}
