package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.LinkedHashMultimap;
import com.lovetropics.minigames.client.lobby.state.ClientBehaviorMap;
import com.lovetropics.minigames.client.lobby.state.ClientQueuedGame;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.CompositeConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.ListConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.SimpleConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.screen.Screen;

public final class GameConfig extends FocusableGui {
	private final Screen screen;
	private final Layout mainLayout;

	private final Handlers handlers;
	
	private ClientQueuedGame configuring;
	private ClientBehaviorMap configData = new ClientBehaviorMap(LinkedHashMultimap.create());
	private final List<INestedGuiEventHandler> children = new ArrayList<>();

	public GameConfig(Screen screen, Layout main, Handlers handlers) {
		this.screen = screen;
		this.mainLayout = main;
		this.handlers = handlers;
	}

	public interface Handlers {

		void saveConfigs();
	}
	
	public void setGame(ClientQueuedGame game) {
		this.configuring = game;
		this.configData = game.configs;
		this.children.clear();
		this.children.add(createWidget(this.configData));
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return children;
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
