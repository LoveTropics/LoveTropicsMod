package com.lovetropics.minigames.client.lobby.screen.game_config;

import java.util.ArrayList;
import java.util.List;

import com.lovetropics.minigames.client.screen.LayoutGui;
import com.lovetropics.minigames.client.screen.LayoutTree;
import com.lovetropics.minigames.client.screen.flex.Axis;
import com.lovetropics.minigames.client.screen.flex.Box;
import com.lovetropics.minigames.client.screen.flex.Layout;
import com.lovetropics.minigames.common.core.game.behavior.config.ConfigData;
import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.IRenderable;
import net.minecraft.client.gui.screen.Screen;

public class ConfigDataUI extends LayoutGui implements IConfigWidget {

	private static class TextLabel implements IRenderable {
		private final Layout renderArea;
		private final String text;
		private final FontRenderer fnt = Minecraft.getInstance().fontRenderer;
		
		TextLabel(LayoutTree ltree, int height, String text) {
			int margin = Math.max((height - fnt.FONT_HEIGHT) / 2, 0);
			ltree.definiteChild(-1, fnt.FONT_HEIGHT, new Box(), new Box(0, margin, 0, margin));
			this.renderArea = ltree.pop();
			this.text = text;
			ltree.pop();
		}

		@Override
		public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			fnt.drawString(matrixStack, text, renderArea.content().left(), renderArea.content().top(), -1);
		}
	}
	
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
		
		float textWidth = 0.35F;
		IConfigWidget widget = GameConfig.createWidget(screen, ltree.child(-textWidth, Axis.X), configs);
		children.add(widget);
		this.label = new TextLabel(ltree.child(textWidth, Axis.X), widget.getHeight(), name);
		this.mainLayout = ltree.pop();
	}

	@Override
	public List<? extends IGuiEventListener> getEventListeners() {
		return children;
	}
	
	@Override
	public int getHeight() {
		return children.stream().mapToInt(IConfigWidget::getHeight).sum();
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
