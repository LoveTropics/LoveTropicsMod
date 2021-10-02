package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;

import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;

import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;

public class ConfigDataUI extends FocusableGui {
	
	private final String name;
	private final ConfigData configs;
	
	private final List<IGuiEventListener> children = new ArrayList<>();

	public ConfigDataUI(String name, ConfigData configs) {
		this.name = name;
		this.configs = configs;
		
		children.add(GameConfig.createWidget(configs));
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return children;
	}

}
