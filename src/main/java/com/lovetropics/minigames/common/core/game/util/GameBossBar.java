package com.lovetropics.minigames.common.core.game.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.server.ServerBossInfo;

public final class GameBossBar implements GameWidget {
	private final ServerBossInfo bar;

	public GameBossBar(ITextComponent title, BossInfo.Color color, BossInfo.Overlay overlay) {
		this.bar = new ServerBossInfo(title, color, overlay);
		this.bar.setDarkenScreen(false);
		this.bar.setCreateWorldFog(false);
		this.bar.setPlayBossMusic(false);
	}

	public void setTitle(ITextComponent title) {
		this.bar.setName(title);
	}

	public void setProgress(float progress) {
		this.bar.setPercent(progress);
	}

	public void setStyle(BossInfo.Color color, BossInfo.Overlay overlay) {
		this.bar.setColor(color);
		this.bar.setOverlay(overlay);
	}

	@Override
	public void addPlayer(ServerPlayerEntity player) {
		this.bar.addPlayer(player);
	}

	@Override
	public void removePlayer(ServerPlayerEntity player) {
		this.bar.removePlayer(player);
	}

	@Override
	public void close() {
		this.bar.removeAllPlayers();
		this.bar.setVisible(false);
	}
}
