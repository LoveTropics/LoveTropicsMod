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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

public final class TimedGameBehavior implements IGameBehavior {
	public static final MapCodec<TimedGameBehavior> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
			Codec.LONG.optionalFieldOf("length", 20L * 60).forGetter(c -> c.length),
			Codec.LONG.optionalFieldOf("close_length", 0L).forGetter(c -> c.closeTime - c.length),
			TemplatedText.CODEC.optionalFieldOf("timer_bar").forGetter(c -> Optional.ofNullable(c.timerBarText)),
			Codec.LONG.optionalFieldOf("countdown_seconds", -1L).forGetter(c -> c.countdownSeconds)
	).apply(i, TimedGameBehavior::new));

	private final long length;
	private final long closeTime;
	@Nullable
	private final TemplatedText timerBarText;
	private final long countdownSeconds;

	@Nullable
	private GameBossBar timerBar;

	private TimedGameState state;

	public TimedGameBehavior(long length, long closeTime, Optional<TemplatedText> timerBar, long countdownSeconds) {
		this.length = length;
		this.closeTime = closeTime;
		timerBarText = timerBar.orElse(null);
		this.countdownSeconds = countdownSeconds;
	}

	@Override
	public void registerState(IGamePhase game, GameStateMap phaseState, GameStateMap instanceState) {
		state = phaseState.register(TimedGameState.KEY, new TimedGameState(length, closeTime));
	}

	@Override
	public void register(IGamePhase game, EventRegistrar events) {
		events.listen(GamePhaseEvents.TICK, () -> tick(game));

		if (timerBarText != null) {
			GlobalGameWidgets widgets = GlobalGameWidgets.registerTo(game, events);
			timerBar = widgets.openBossBar(CommonComponents.EMPTY, BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.NOTCHED_10);
		}

		game.controlCommands().add("pause", ControlCommand.forInitiator(source -> state.setPaused(true)));
		game.controlCommands().add("unpause", ControlCommand.forInitiator(source -> state.setPaused(false)));
	}

	private void tick(IGamePhase game) {
		switch (state.tick(game.ticks())) {
			case RUNNING -> tickRunning(game);
			case GAME_OVER -> game.invoker(GameLogicEvents.GAME_OVER).onGameOver();
			case CLOSE -> game.requestStop(GameStopReason.finished());
		}
	}

	private void tickRunning(IGamePhase game) {
		long ticksRemaining = state.getTicksRemaining();
		if (ticksRemaining % SharedConstants.TICKS_PER_SECOND == 0) {
			long secondsRemaining = ticksRemaining / SharedConstants.TICKS_PER_SECOND;

			if (secondsRemaining <= countdownSeconds) {
				playCountdownSound(game, secondsRemaining);
			}

			if (timerBar != null) {
				timerBar.setTitle(getTimeRemainingText(game, ticksRemaining));
				timerBar.setProgress((float) ticksRemaining / length);
			}
		}
	}

	private void playCountdownSound(IGamePhase game, long seconds) {
		float pitch = seconds == 0 ? 1.5F : 1.0F;
		game.allPlayers().playSound(SoundEvents.ARROW_HIT_PLAYER, SoundSource.MASTER, 0.8F, pitch);
	}

	private Component getTimeRemainingText(IGamePhase game, long ticksRemaining) {
		long secondsRemaining = ticksRemaining / SharedConstants.TICKS_PER_SECOND;

		Component timeText = Component.literal(Util.formatMinutesSeconds(secondsRemaining)).withStyle(ChatFormatting.AQUA);
		Component gameNameText = game.definition().name().copy().withStyle(ChatFormatting.AQUA);

		return timerBarText.apply(Map.of("time", timeText, "game", gameNameText));
	}
}
