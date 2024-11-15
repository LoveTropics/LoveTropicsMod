package com.lovetropics.minigames.common.core.game.util;

import com.lovetropics.minigames.common.core.game.IGamePhase;
import com.lovetropics.minigames.common.core.game.behavior.event.EventRegistrar;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePhaseEvents;
import com.lovetropics.minigames.common.core.game.behavior.event.GamePlayerEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.DyeColor;

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

	public GameSidebar openSidebar(Component title) {
		return registerWidget(new GameSidebar(game.server(), title));
	}

	public GameBossBar openBossBar(Component title, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
		return registerWidget(new GameBossBar(title, color, overlay));
	}

	public GameBossBar openBossBar(Component title, DyeColor color, BossEvent.BossBarOverlay overlay) {
		BossEvent.BossBarColor bossBarColor = switch (color) {
			case WHITE, GRAY, LIGHT_GRAY -> BossEvent.BossBarColor.WHITE;
			case ORANGE, RED -> BossEvent.BossBarColor.RED;
			case MAGENTA, BLACK, PURPLE -> BossEvent.BossBarColor.PURPLE;
			case LIGHT_BLUE, BLUE, CYAN -> BossEvent.BossBarColor.BLUE;
			case YELLOW, BROWN -> BossEvent.BossBarColor.YELLOW;
			case LIME, GREEN -> BossEvent.BossBarColor.GREEN;
			case PINK -> BossEvent.BossBarColor.PINK;
		};
		return openBossBar(title, bossBarColor, overlay);
	}

	private <T extends GameWidget> T registerWidget(T widget) {
		game.lobby().getPlayers().forEach(widget::addPlayer);
		widgets.add(widget);
		return widget;
	}

	private void addPlayer(ServerPlayer player) {
		for (GameWidget widget : widgets) {
			widget.addPlayer(player);
		}
	}

	private void removePlayer(ServerPlayer player) {
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
