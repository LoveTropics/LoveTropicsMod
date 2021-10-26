package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;

import java.util.ArrayList;
import java.util.List;

public final class GlobalGameWidgets {
	private final IGamePhase game;

	private final List<GameWidget> widgets = new ArrayList<>();

	private GlobalGameWidgets(IGamePhase game) {
		this.game = game;
	}

	public static GlobalGameWidgets registerTo(IGamePhase game, EventRegistrar events) {
		GlobalGameWidgets widgets = new GlobalGameWidgets(game);
		events.listen(GamePlayerEvents.ADD, widgets::addPlayer);
		events.listen(GamePlayerEvents.REMOVE, widgets::removePlayer);
		events.listen(GamePhaseEvents.DESTROY, widgets::close);

		return widgets;
	}

	public GameSidebar openSidebar(ITextComponent title) {
		return registerWidget(new GameSidebar(game.getServer(), title));
	}

	public GameBossBar openBossBar(ITextComponent title, BossInfo.Color color, BossInfo.Overlay overlay) {
		return registerWidget(new GameBossBar(title, color, overlay));
	}

	private <T extends GameWidget> T registerWidget(T widget) {
		game.getLobby().getPlayers().forEach(widget::addPlayer);
		widgets.add(widget);
		return widget;
	}

	private void addPlayer(ServerPlayerEntity player) {
		for (GameWidget widget : widgets) {
			widget.addPlayer(player);
		}
	}

	private void removePlayer(ServerPlayerEntity player) {
		for (GameWidget widget : widgets) {
			widget.removePlayer(player);
		}
	}

	private void close() {
		for (GameWidget widget : widgets) {
			widget.close();
		}
	}
}
