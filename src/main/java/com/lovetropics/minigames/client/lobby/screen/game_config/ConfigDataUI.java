package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;

import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;

public class ConfigDataUI<T extends INestedGuiEventHandler & IRenderable> extends LayoutGui {
	
	private final String name;
	private final ConfigData configs;
	
	private final List<T> children = new ArrayList<>();

	public ConfigDataUI(Screen screen, Layout layout, String name, ConfigData configs) {
		super(layout);
		this.name = name;
		this.configs = configs;
		
		// TODO narrow layout
		children.add(GameConfig.createWidget(layout, configs));
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return children;
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		for (T child : children) {
			child.render(matrixStack, mouseX, mouseY, partialTicks);
		}
	}

}
