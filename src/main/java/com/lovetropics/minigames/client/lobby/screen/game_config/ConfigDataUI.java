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
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.TranslationTextComponent;

public class ConfigDataUI extends LayoutGui implements IConfigWidget {

	private final Screen screen;
	private final String name;
	private final ConfigData configs;
	private final TextLabel label;
	
	private final List<IConfigWidget> children = new ArrayList<>();

	public ConfigDataUI(Screen screen, LayoutTree ltree, String name, ConfigData configs) {
		super();
		this.screen = screen;
		this.name = name;
		this.configs = configs;
		

		this.label = new TextLabel(ltree.child(1, Axis.X), 11, new TranslationTextComponent(name), Align.Cross.START, Align.Cross.START);
		IConfigWidget widget = GameConfig.createWidget(screen, ltree.child(1, Axis.X), configs);
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
		for (IConfigWidget child : children) {
			child.render(matrixStack, mouseX, mouseY, partialTicks);
		}
	}
}
