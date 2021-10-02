package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.lovetropics.minigames.client.lobby.manage.screen.ManageLobbyScreen;
import com.lovetropics.minigames.client.lobby.manage.state.ClientLobbyQueuedGame;
import com.lovetropics.minigames.client.lobby.state.ClientBehaviorMap;
import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.CompositeConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.ListConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.SimpleConfigData;

import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.screen.Screen;

public final class GameConfig extends FocusableGui {
	private final Screen screen;
	private final Layout mainLayout;

	private final Handlers handlers;
	
	private ClientLobbyQueuedGame configuring;
	private ClientBehaviorMap configData = new ClientBehaviorMap(LinkedHashMultimap.create());
	private final Multimap<GameBehaviorType<?>, BehaviorConfigUI> children = LinkedHashMultimap.create();

	public GameConfig(Screen screen, Layout main, Handlers handlers) {
		this.screen = screen;
		this.mainLayout = main;
		this.handlers = handlers;
	}

	public interface Handlers {

		void saveConfigs();
	}
	
	public void setGame(ClientLobbyQueuedGame game) {
		this.configuring = game;
		this.configData = game.configs();
		this.children.clear();
		for (Entry<GameBehaviorType<?>, ClientConfigList> e : configData.behaviors.entries()) {
			this.children.put(e.getKey(), new BehaviorConfigUI((ManageLobbyScreen) screen, mainLayout, e.getKey(), e.getValue()));
		}
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return Lists.newArrayList(children.values());
	}

	public static INestedGuiEventHandler createWidget(ConfigData value) {
		if (value instanceof SimpleConfigData) {
			return SimpleConfigWidget.from((SimpleConfigData) value);
		} else if (value instanceof ListConfigData) {
			return ListConfigWidget.from((ListConfigData) value);
		} else if (value instanceof CompositeConfigData) {
			return CompositeConfigWidget.from((CompositeConfigData) value);
		}
		throw new IllegalArgumentException("Unknown config type: " + value);
	}
}
