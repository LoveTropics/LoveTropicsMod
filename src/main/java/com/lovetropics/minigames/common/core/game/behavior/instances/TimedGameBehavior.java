package com.lovetropics.minigames.common.core.game.behavior.instances;

import com.lovetropics.minigames.common.core.game.GameManager;
import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.PlayerRole;
import com.lovetropics.minigames.common.core.game.behavior.IGameBehavior;
import com.lovetropics.minigames.common.core.game.behavior.event.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.BossInfo;
import net.minecraft.world.server.ServerBossInfo;

public final class TimedGameBehavior implements IGameBehavior {
	public static final Codec<TimedGameBehavior> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.LONG.optionalFieldOf("length", 20L * 60).forGetter(c -> c.length),
				Codec.LONG.optionalFieldOf("close_length", 0L).forGetter(c -> c.closeTime - c.length),
				Codec.BOOL.optionalFieldOf("timer_bar", false).forGetter(c -> c.timerBar != null)
		).apply(instance, TimedGameBehavior::new);
	});

	private final long length;
	private final long closeTime;
	private final ServerBossInfo timerBar;

	public TimedGameBehavior(long length, long closeTime, boolean timerBar) {
		this.length = length;
		this.closeTime = length + closeTime;
		this.timerBar = timerBar ? new ServerBossInfo(new StringTextComponent(""), BossInfo.Color.GREEN, BossInfo.Overlay.PROGRESS) : null;
	}

	@Override
	public void register(IGameInstance registerGame, GameEventListeners events) {
		events.listen(GamePlayerEvents.JOIN, this::onPlayerJoin);
		events.listen(GamePlayerEvents.LEAVE, this::onPlayerLeave);
		events.listen(GameLifecycleEvents.FINISH, this::onFinish);
		events.listen(GameLifecycleEvents.TICK, this::onTick);
	}

	// TODO: GameBossBar which accepts these events
	private void onPlayerJoin(IGameInstance game, ServerPlayerEntity player, PlayerRole role) {
		if (this.timerBar != null) {
			this.timerBar.addPlayer(player);
		}
	}

	private void onPlayerLeave(IGameInstance game, ServerPlayerEntity player) {
		if (this.timerBar != null) {
			this.timerBar.removePlayer(player);
		}
	}

	private void onFinish(IGameInstance game) {
		if (this.timerBar != null) {
			this.timerBar.removeAllPlayers();
			this.timerBar.setVisible(false);
		}
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
			timerBar.setName(this.getTimeRemainingText(ticksRemaining));
			timerBar.setPercent((float) ticksRemaining / length);
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
