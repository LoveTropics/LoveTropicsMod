package com.lovetropics.minigames.common.core.game.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerBossEvent;

public final class GameBossBar implements GameWidget {
	private static final float UPDATE_PROGRESS_THRESHOLD = 0.025f;

	private final ServerBossEvent bar;

	public GameBossBar(Component title, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
		this.bar = new ServerBossEvent(title, color, overlay);
		this.bar.setDarkenScreen(false);
		this.bar.setCreateWorldFog(false);
		this.bar.setPlayBossMusic(false);
	}

	public void setTitle(Component title) {
		this.bar.setName(title);
	}

	public void setProgress(float progress) {
		if (Math.abs(progress - bar.getProgress()) > UPDATE_PROGRESS_THRESHOLD) {
			this.bar.setProgress(progress);
		}
	}

	public void setStyle(BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
		this.bar.setColor(color);
		this.bar.setOverlay(overlay);
	}

	@Override
	public void addPlayer(ServerPlayer player) {
		this.bar.addPlayer(player);
	}

	@Override
	public void removePlayer(ServerPlayer player) {
		this.bar.removePlayer(player);
	}

	@Override
	public void close() {
		this.bar.removeAllPlayers();
		this.bar.setVisible(false);
	}
}
