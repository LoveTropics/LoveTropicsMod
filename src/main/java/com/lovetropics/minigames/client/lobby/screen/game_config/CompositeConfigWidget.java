package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.CompositeConfigData;

import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;

public class CompositeConfigWidget extends FocusableGui {
	
	private final List<INestedGuiEventHandler> children = new ArrayList<>();
	
	public static CompositeConfigWidget from(CompositeConfigData data) {
		CompositeConfigWidget ret = new CompositeConfigWidget();
		for (Map.Entry<String, ConfigData> e : data.value().entrySet()) {
			ret.children.add(GameConfig.createWidget(e.getValue()));
		}
		return ret;
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return children;
	}
}
