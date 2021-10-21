package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.List;

import com.google.common.collect.Lists;
import com.lovetropics.minigames.client.lobby.manage.screen.ManageLobbyScreen;
import com.lovetropics.minigames.client.lobby.state.ClientConfigList;
import com.lovetropics.minigames.client.screen.DynamicLayoutGui;
import com.lovetropics.minigames.client.screen.flex.Flex;
import com.lovetropics.minigames.common.core.game.behavior.GameBehaviorType;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;

public class BehaviorConfigUI extends DynamicLayoutGui {
	
	private final GameBehaviorType<?> behavior;
	private final ClientConfigList configs;
	
	private final BehaviorConfigList list;

	public BehaviorConfigUI(ManageLobbyScreen screen, Flex basis, GameBehaviorType<?> behavior, ClientConfigList configs) {
		super(basis);
		this.behavior = behavior;
		this.configs = configs;
		
		this.list = new BehaviorConfigList(screen, basis, configs);
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return Lists.newArrayList(list);
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		list.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	public int getHeight() {
		return list.getHeight();
	}
}
