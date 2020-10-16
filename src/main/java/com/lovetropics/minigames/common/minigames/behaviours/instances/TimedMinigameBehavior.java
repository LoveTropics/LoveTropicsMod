package com.lovetropics.minigames.common.minigames.behaviours.instances;

import com.lovetropics.minigames.common.minigames.IMinigameInstance;
import com.lovetropics.minigames.common.minigames.MinigameManager;
import com.lovetropics.minigames.common.minigames.PlayerRole;
import com.lovetropics.minigames.common.minigames.behaviours.IMinigameBehavior;
import com.mojang.datafixers.Dynamic;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerBossInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TimedMinigameBehavior implements IMinigameBehavior {
	private final long length;
	private final long closeTime;
	private final ServerBossInfo timerBar;

	private final List<Consumer<IMinigameInstance>> finishListeners = new ArrayList<>();

	public TimedMinigameBehavior(long length, long closeTime, boolean timerBar) {
		this.length = length;
		this.closeTime = length + closeTime;
		this.timerBar = timerBar ? new ServerBossInfo(new StringTextComponent(""), BossInfo.Color.GREEN, BossInfo.Overlay.PROGRESS) : null;
	}

	public static <T> TimedMinigameBehavior parse(Dynamic<T> root) {
		long length = root.get("length").asLong(20 * 60);
		long closeLength = root.get("close_length").asLong(0);
		// TODO: we store this in a string because DFU doesn't handle booleans at the moment
		boolean timerBar = root.get("timer_bar").asString("false").equalsIgnoreCase("true");
		return new TimedMinigameBehavior(length, closeLength, timerBar);
	}

	public void onFinish(Consumer<IMinigameInstance> listener) {
		this.finishListeners.add(listener);
	}

	@Override
	public void onPlayerJoin(IMinigameInstance minigame, ServerPlayerEntity player, PlayerRole role) {
		if (this.timerBar != null) {
			this.timerBar.addPlayer(player);
		}
	}

	@Override
	public void onPlayerLeave(IMinigameInstance minigame, ServerPlayerEntity player) {
		if (this.timerBar != null) {
			this.timerBar.removePlayer(player);
		}
	}

	@Override
	public void onFinish(IMinigameInstance minigame) {
		if (this.timerBar != null) {
			this.timerBar.removeAllPlayers();
			this.timerBar.setVisible(false);
		}
	}

	@Override
	public void worldUpdate(IMinigameInstance minigame, World world) {
		long ticks = minigame.ticks();
		if (ticks >= closeTime) {
			MinigameManager.getInstance().finish();
			return;
		}

		if (ticks == length) {
			for (Consumer<IMinigameInstance> listener : finishListeners) {
				listener.accept(minigame);
			}
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

		return new StringTextComponent("Time Remaining: " + time + "...");
	}
}
