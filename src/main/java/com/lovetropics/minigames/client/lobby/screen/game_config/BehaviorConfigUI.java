package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.List;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.client.lobby.manage.screen.ManageLobbyScreen;
import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.LayoutTree;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FocusableGui;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;

public class BehaviorConfigUI extends FocusableGui implements IRenderable {
	
	private final GameBehaviorType<?> behavior;
	private final ClientConfigList configs;
	
	private final BehaviorConfigList list;

	public BehaviorConfigUI(ManageLobbyScreen screen, LayoutTree lstack, GameBehaviorType<?> behavior, ClientConfigList configs) {
		super();
		this.behavior = behavior;
		this.configs = configs;
		
		this.list = new BehaviorConfigList(screen, lstack, configs);
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return Lists.newArrayList(list);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		list.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	public int getHeight() {
		return list.getHeight();
	}
}
