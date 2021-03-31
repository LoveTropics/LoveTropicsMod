package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameManager;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLogicEvents;
import com.lovetropics.minigames.common.core.game.util.GameBossBar;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.BossInfo;

public final class TimedGameBehavior implements IGameBehavior {
	public static final Codec<TimedGameBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.optionalFieldOf("length", 20L * 60).forGetter(c -> c.length),
				Codec.LONG.optionalFieldOf("close_length", 0L).forGetter(c -> c.closeTime - c.length),
				Codec.BOOL.optionalFieldOf("timer_bar", false).forGetter(c -> c.hasTimerBar)
		).apply(instance, TimedGameBehavior::new);
	});

	private final long length;
	private final long closeTime;
	private final boolean hasTimerBar;

	private GameBossBar timerBar;

	public TimedGameBehavior(long length, long closeTime, boolean timerBar) {
		this.length = length;
		this.closeTime = length + closeTime;
		this.hasTimerBar = timerBar;
	}

	@Override
	public void register(IGameInstance game, GameEventListeners events) {
		events.listen(GameLifecycleEvents.TICK, this::onTick);

		timerBar = hasTimerBar ? GameBossBar.openGlobal(game, new StringTextComponent(""), BossInfo.Color.GREEN, BossInfo.Overlay.PROGRESS) : null;
	}

	private void onTick(IGameInstance game) {
		long ticks = game.ticks();
		if (ticks >= closeTime) {
			GameManager.get().finish();
			return;
		}

		if (ticks == length) {
			game.invoker(GameLogicEvents.GAME_OVER).onGameOver(game);
		}

		if (ticks % 20 == 0 && timerBar != null) {
			long ticksRemaining = Math.max(length - ticks, 0);
			timerBar.setTitle(this.getTimeRemainingText(ticksRemaining));
			timerBar.setProgress((float) ticksRemaining / length);
		}
	}

	private ITextComponent getTimeRemainingText(long ticksRemaining) {
		long secondsRemaining = ticksRemaining / 20;

		long minutes = secondsRemaining / 60;
		long seconds = secondsRemaining % 60;
		String time = String.format("%02d:%02d", minutes, seconds);

		return new StringTextComponent("Time Remaining: ")
				.appendSibling(new StringTextComponent(time).mergeStyle(TextFormatting.GRAY))
				.appendString("...");
	}
}
