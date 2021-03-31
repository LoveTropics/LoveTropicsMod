package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.common.core.game.IGameInstance;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraft.world.server.ServerBossInfo;

public final class GameBossBar implements AutoCloseable {
	private final ServerBossInfo bar;

	public GameBossBar(ITextComponent title, BossInfo.Color color, BossInfo.Overlay overlay) {
		this.bar = new ServerBossInfo(title, color, overlay);
		this.bar.setDarkenSky(false);
		this.bar.setCreateFog(false);
		this.bar.setPlayEndBossMusic(false);
	}

	public static GameBossBar openGlobal(IGameInstance game, ITextComponent title, BossInfo.Color color, BossInfo.Overlay overlay) {
		GameBossBar widget = new GameBossBar(title, color, overlay);
		for (ServerPlayerEntity player : game.getAllPlayers()) {
			widget.addPlayer(player);
		}

		GameEventListeners events = game.getEvents();
		events.listen(GamePlayerEvents.JOIN, (g, player, role) -> widget.addPlayer(player));
		events.listen(GamePlayerEvents.LEAVE, (g, player) -> widget.removePlayer(player));
		events.listen(GameLifecycleEvents.FINISH, g -> widget.close());

		return widget;
	}

	public static GameBossBar openGlobal(IGameInstance game, ITextComponent title) {
		return openGlobal(game, title, BossInfo.Color.PURPLE, BossInfo.Overlay.PROGRESS);
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

	public void addPlayer(ServerPlayerEntity player) {
		this.bar.addPlayer(player);
	}

	public void removePlayer(ServerPlayerEntity player) {
		this.bar.removePlayer(player);
	}

	@Override
	public void close() {
		this.bar.removeAllPlayers();
		this.bar.setVisible(false);
	}
}
