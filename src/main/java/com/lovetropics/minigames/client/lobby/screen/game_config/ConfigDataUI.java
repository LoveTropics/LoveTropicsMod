package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;

import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.LayoutTree;
import com.lovetropics.minigames.client.screen.flex.Align;
import com.lovetropics.minigames.client.screen.flex.Axis;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.INestedGuiEventHandler;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

public class ConfigDataUI extends LayoutGui implements IConfigWidget {

	private final GameConfig parent;
	private final String name;
	private final ConfigData configs;
	private final TextLabel label;
	
	private final List<INestedGuiEventHandler> children = new ArrayList<>();

	public ConfigDataUI(GameConfig parent, LayoutTree ltree, String name, ConfigData configs) {
		super();
		this.parent = parent;
		this.name = name;
		this.configs = configs;
		

		this.label = new TextLabel(ltree.child(1, Axis.X), 11, new TranslationTextComponent(name), Align.Cross.START, Align.Cross.START);
		IConfigWidget widget = parent.createWidget(ltree.child(1, Axis.X), configs);
		children.add(widget);
		this.mainLayout = ltree.pop();
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return children;
	}
	
	@Override
	public int getHeight() {
		return this.mainLayout.background().height();
	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		this.label.render(matrixStack, mouseX, mouseY, partialTicks);
		for (INestedGuiEventHandler child : children) {
			if (child instanceof IRenderable) {
				((IRenderable)child).render(matrixStack, mouseX, mouseY, partialTicks);
			}
		}
	}
}
