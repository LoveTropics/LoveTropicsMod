package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;

import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData.ListConfigData;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigType;

import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;

public class ListConfigWidget extends FocusableGui {
	
	private final List<INestedGuiEventHandler> children = new ArrayList<>();
	
	public static ListConfigWidget from(ListConfigData data) {
		ListConfigWidget ret = new ListConfigWidget();
		if (data.type() == ConfigType.COMPOSITE) {
			for (Object val : data.value()) {
				ret.children.add(GameConfig.createWidget((ConfigData) val));
			}
		} else {
			// TODO others?
		}
		return ret;
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return children;
	}
}
