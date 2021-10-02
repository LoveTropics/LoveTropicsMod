package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.List;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.client.lobby.manage.screen.ManageLobbyScreen;
import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;

import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;

public class BehaviorConfigUI extends FocusableGui {
	
	private final GameBehaviorType<?> behavior;
	private final ClientConfigList configs;
	
	private final BehaviorConfigList list;

	public BehaviorConfigUI(ManageLobbyScreen screen, Layout layout, GameBehaviorType<?> behavior, ClientConfigList configs) {
		this.behavior = behavior;
		this.configs = configs;
		
		this.list = new BehaviorConfigList(screen, layout, configs);
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return Lists.newArrayList(list);
	}
}
