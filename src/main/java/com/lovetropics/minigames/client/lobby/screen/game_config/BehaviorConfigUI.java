package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.List;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.client.lobby.manage.screen.ManageLobbyScreen;
import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;

public class BehaviorConfigUI extends LayoutGui {
	
	private final GameBehaviorType<?> behavior;
	private final ClientConfigList configs;
	
	private final BehaviorConfigList list;

	public BehaviorConfigUI(ManageLobbyScreen screen, Layout layout, GameBehaviorType<?> behavior, ClientConfigList configs) {
		super(layout);
		this.behavior = behavior;
		this.configs = configs;
		
		// TODO narrow layout
		this.list = new BehaviorConfigList(screen, layout, configs);
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return Lists.newArrayList(list);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		list.render(matrixStack, mouseX, mouseY, partialTicks);
	}
}
