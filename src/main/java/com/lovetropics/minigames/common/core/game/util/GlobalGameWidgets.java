package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.common.core.game.IActiveGame;
import com.lovetropics.minigames.common.core.game.behavior.event.GameEventListeners;
import com.lovetropics.minigames.common.core.game.behavior.event.GameLifecycleEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;

public final class GlobalGameWidgets {
	private final IActiveGame game;

	public GlobalGameWidgets(IActiveGame game) {
		this.game = game;
	}

	public GameSidebar openSidebar(ITextComponent title) {
		return registerWidget(new GameSidebar(game.getServer(), title));
	}

	public GameBossBar openBossBar(ITextComponent title, BossInfo.Color color, BossInfo.Overlay overlay) {
		return registerWidget(new GameBossBar(title, color, overlay));
	}

	private <T extends GameWidget> T registerWidget(T widget) {
		GameEventListeners events = game.getEvents();

		for (ServerPlayerEntity player : game.getAllPlayers()) {
			widget.addPlayer(player);
		}

		events.listen(GamePlayerEvents.JOIN, (g, player, role) -> widget.addPlayer(player));
		events.listen(GamePlayerEvents.LEAVE, (g, player) -> widget.removePlayer(player));
		events.listen(GameLifecycleEvents.STOP, g -> widget.close());

		return widget;
	}
}
